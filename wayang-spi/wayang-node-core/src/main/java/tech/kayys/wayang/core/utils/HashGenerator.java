
/**
 * Hash generator for integrity checking
 */
@ApplicationScoped
public class HashGenerator {
    
    public String hash(Object obj) {
        try {
            var json = JsonUtils.toJson(obj);
            var digest = MessageDigest.getInstance("SHA-256");
            var hashBytes = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        var hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
