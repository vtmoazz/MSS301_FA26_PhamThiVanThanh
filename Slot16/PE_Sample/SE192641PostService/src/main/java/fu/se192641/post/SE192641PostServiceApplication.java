package fu.se192641.post;

import fu.se192641.post.common.DateUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.TimeZone;

@SpringBootApplication
@EnableFeignClients
public class SE192641PostServiceApplication {

    public static void main(String[] args) {
        // JSON (dd/MM/yyyy) và JDBC cùng dùng một múi giờ, nên ngày không bị lệch khi lưu và đọc lại.
        TimeZone.setDefault(TimeZone.getTimeZone(DateUtils.ZONE));
        SpringApplication.run(SE192641PostServiceApplication.class, args);
    }
}
