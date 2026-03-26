// 密码哈希生成工具
// 如果需要生成新的密码哈希，可以运行这个 Java 代码
// 或者使用在线 BCrypt 生成器：https://bcrypt-generator.com/

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成密码哈希
        System.out.println("password123 => " + encoder.encode("password123"));
        System.out.println("teacher123 => " + encoder.encode("teacher123"));
    }
}
