package tech.kayys.wayang.security;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


public enum UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    DELETED,
    PENDING_VERIFICATION,
    PASSWORD_RESET
}
