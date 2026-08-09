package tech.kayys.wayang.context.impl;

import tech.kayys.wayang.context.api.Skeletonizer;
import tech.kayys.wayang.context.api.exception.SkeletonizationException;
import tech.kayys.wayang.context.api.model.SkeletonResult;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Strips method and constructor bodies down to a minimal, still-syntactically-
 * valid stub. Every output level of this class narrows *scope* -- which
 * members are shown -- never *fidelity*: no method that survives any of
 * these three methods has its signature, Javadoc, or annotations rewritten,
 * only its body replaced with a trivial stub or its presence omitted.
 */
public final class JavaParserSkeletonizer implements Skeletonizer {

    @Override
    public SkeletonResult skeletonize(String source) {
        CompilationUnit cu = parseOrThrow(source, "skeletonization");

        int[] stripped = {0};

        cu.findAll(InitializerDeclaration.class).forEach(Node::remove);

        cu.findAll(MethodDeclaration.class).forEach(method -> {
            if (method.getBody().isPresent()) {
                method.setBody(stubBody(method.getType()));
                stripped[0]++;
            }
        });

        cu.findAll(ConstructorDeclaration.class).forEach(ctor -> {
            ctor.setBody(new BlockStmt());
            stripped[0]++;
        });

        cu.findAll(FieldDeclaration.class).forEach(field ->
                field.getVariables().forEach(v -> v.removeInitializer()));

        return new SkeletonResult(cu.toString(), stripped[0]);
    }

    @Override
    public SkeletonResult skeletonizePruned(String source, Set<String> keepMemberNames) {
        CompilationUnit cu = parseOrThrow(source, "pruned skeletonization");

        int[] strippedBodies = {0};
        Map<String, Integer> omittedPerType = new LinkedHashMap<>();

        cu.findAll(InitializerDeclaration.class).forEach(Node::remove);

        for (MethodDeclaration method : new ArrayList<>(cu.findAll(MethodDeclaration.class))) {
            if (keepMemberNames.contains(method.getNameAsString())) {
                if (method.getBody().isPresent()) {
                    method.setBody(stubBody(method.getType()));
                    strippedBodies[0]++;
                }
            } else {
                // capture the owning type before remove() detaches the node
                method.findAncestor(ClassOrInterfaceDeclaration.class)
                        .ifPresent(owner -> omittedPerType.merge(owner.getNameAsString(), 1, Integer::sum));
                method.remove();
            }
        }

        cu.findAll(ConstructorDeclaration.class).forEach(ctor -> {
            ctor.setBody(new BlockStmt());
            strippedBodies[0]++;
        });

        cu.findAll(FieldDeclaration.class).forEach(field ->
                field.getVariables().forEach(v -> v.removeInitializer()));

        String printed = cu.toString();
        for (Map.Entry<String, Integer> entry : omittedPerType.entrySet()) {
            printed = annotateOmission(printed, entry.getKey(), entry.getValue());
        }

        return new SkeletonResult(printed, strippedBodies[0]);
    }

    @Override
    public String digest(String source) {
        CompilationUnit cu = parseOrThrow(source, "digest");

        cu.getImports().clear();
        cu.findAll(InitializerDeclaration.class).forEach(Node::remove);

        cu.findAll(MethodDeclaration.class).forEach(method -> {
            if (method.getBody().isPresent()) {
                method.setBody(stubBody(method.getType()));
            }
            method.getAnnotations().clear();
            method.removeComment();
        });

        cu.findAll(ConstructorDeclaration.class).forEach(ctor -> {
            ctor.setBody(new BlockStmt());
            ctor.getAnnotations().clear();
            ctor.removeComment();
        });

        cu.findAll(FieldDeclaration.class).forEach(field -> {
            field.getVariables().forEach(v -> v.removeInitializer());
            field.getAnnotations().clear();
            field.removeComment();
        });

        cu.findAll(TypeDeclaration.class).forEach(type -> {
            type.getAnnotations().clear();
            type.removeComment();
        });

        cu.getAllContainedComments().forEach(Comment::remove);

        return cu.toString();
    }

    private CompilationUnit parseOrThrow(String source, String phase) {
        try {
            return StaticJavaParser.parse(source);
        } catch (ParseProblemException e) {
            throw new SkeletonizationException("could not parse source for " + phase, e);
        }
    }

    /** Inserts a one-line, non-AST omission note right after a type's opening brace. */
    private String annotateOmission(String printed, String typeName, int omittedCount) {
        Pattern p = Pattern.compile(
                "((?:class|interface|enum|record)\\s+" + Pattern.quote(typeName) + "\\b[^{]*\\{)");
        Matcher m = p.matcher(printed);
        if (!m.find()) return printed;
        String note = "\n    // pruned: " + omittedCount + " member(s) not reachable from target, omitted";
        return printed.substring(0, m.end()) + note + printed.substring(m.end());
    }

    private BlockStmt stubBody(Type returnType) {
        BlockStmt block = new BlockStmt();
        if (returnType instanceof VoidType) {
            return block;
        }
        block.addStatement(new ReturnStmt(defaultValueFor(returnType)));
        return block;
    }

    private Expression defaultValueFor(Type type) {
        if (type instanceof PrimitiveType primitive) {
            String literal = switch (primitive.getType()) {
                case BOOLEAN -> "false";
                case CHAR -> "'\\u0000'";
                case BYTE, SHORT, INT -> "0";
                case LONG -> "0L";
                case FLOAT -> "0.0f";
                case DOUBLE -> "0.0";
            };
            return StaticJavaParser.parseExpression(literal);
        }
        return StaticJavaParser.parseExpression("null");
    }
}
