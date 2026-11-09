package com.fenglin.springboottest.config;

import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 启动期初始化：
 * 1) 自动创建演示账号 admin / 123456（BCrypt 加密）；
 * 2) 老库增量迁移：world_character 补 avatar 头像列（新库由 schema.sql 直接建出）。
 * <p>注：接取 / 报名记录的「完成状态」列迁移放在 schema.sql 里做，
 * 因为 data.sql 的种子数据已带 points 列，必须保证补列先于数据脚本执行。</p>
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (userService.lambdaQuery().eq(User::getUsername, "admin").count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("123456"));
            admin.setNickname("星河守护者");
            admin.setCreatedAt(LocalDateTime.now());
            userService.save(admin);
        }
        migrateCharacterAvatarColumn();
    }

    /** 22 位人物的默认头像（图片文件在前端 public/avatars/ 下） */
    private static final Map<String, String> DEFAULT_AVATARS = Map.ofEntries(
            Map.entry("石昊", "/avatars/shihao.jpg"),
            Map.entry("柳神", "/avatars/liushen.jpg"),
            Map.entry("小塔", "/avatars/xiaota.jpg"),
            Map.entry("曹雨生", "/avatars/caoyusheng.jpg"),
            Map.entry("火灵儿", "/avatars/huolinger.jpg"),
            Map.entry("云曦", "/avatars/yunxi.jpg"),
            Map.entry("秦昊", "/avatars/qinhao.jpg"),
            Map.entry("石中天", "/avatars/shizhongtian.jpg"),
            Map.entry("天角蚁", "/avatars/tianjiaoyi.jpg"),
            Map.entry("神蚕公主", "/avatars/shencan.jpg"),
            Map.entry("黄金狮子", "/avatars/huangjingshizi.jpg"),
            Map.entry("孟天正", "/avatars/mengtianzheng.jpg"),
            Map.entry("真凰", "/avatars/zhenhuang.jpg"),
            Map.entry("鲲鹏", "/avatars/kunpeng.jpg"),
            Map.entry("火皇", "/avatars/huohuang.jpg"),
            Map.entry("麒麟", "/avatars/qilin.jpg"),
            Map.entry("大魔神", "/avatars/damoshen.jpg"),
            Map.entry("狻猊", "/avatars/suanni.jpg"),
            Map.entry("月婵", "/avatars/yuechan.jpg"),
            Map.entry("石毅", "/avatars/shiyi.jpg"),
            Map.entry("秦怡宁", "/avatars/qinyining.jpg"),
            Map.entry("石云峰", "/avatars/shiyunfeng.jpg"));

    /** world_character 老库没有 avatar 列时自动补上，并给 22 位人物回填默认头像（幂等） */
    private void migrateCharacterAvatarColumn() {
        try {
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'world_character' AND COLUMN_NAME = 'avatar'",
                    Integer.class);
            if (cnt != null && cnt == 0) {
                jdbcTemplate.execute("ALTER TABLE world_character ADD COLUMN avatar VARCHAR(255) DEFAULT NULL "
                        + "COMMENT '头像图片路径（如 /avatars/shihao.jpg）' AFTER glyph");
            }
            DEFAULT_AVATARS.forEach((name, path) -> jdbcTemplate.update(
                    "UPDATE world_character SET avatar = ? WHERE name = ? AND (avatar IS NULL OR avatar = '')",
                    path, name));
        } catch (Exception ignored) {
            // 迁移失败不阻塞启动（如权限不足），业务仍可按旧字段运行
        }
    }
}
