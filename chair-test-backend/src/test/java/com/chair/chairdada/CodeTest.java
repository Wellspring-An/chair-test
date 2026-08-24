package com.chair.chairdada;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chair.chairdada.mapper.UserMapper;
import com.chair.chairdada.model.entity.User;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.chair.chairdada.config.TokenConfig.SALT;

@Log4j
@SpringBootTest(classes = MainApplication.class)
public class CodeTest {

//    @Resource
//    DeepSeekUtil deepSeekUtil;

    @Test
    public void test() throws IOException {
//        DeepSeekUtil.askDeepSeekSSE("你好，你能做什么？").subscribe(
//                chunk -> System.out.print(chunk), // 逐段输出
//                error -> System.err.println("Error: " + error),
//                () -> System.out.println("\nStream completed!")
//        );
//        WebClient client = WebClient.create("http://localhost:11434");
//
//        // 流式调用计算方法
//        Flux<String> stream = client.get()
//                .uri(uriBuilder -> uriBuilder.path("/api/generate")
//                        .queryParam("model", "deepseek-r1:1.5b")
//                        .queryParam("prompt", "你好，你能做什么？")
//                        .queryParam("stream", true)
//                        .build())
//                .accept(MediaType.TEXT_EVENT_STREAM)
//                .retrieve()
//                .bodyToFlux(String.class);
//
//        // 订阅流式响应
//        stream.subscribe(
//                chunk -> System.out.print(chunk),  // 逐字输出
//                error -> System.err.println("错误: " + error.getMessage()),
//                () -> System.out.println("\n计算完成！")
//        );
//
//        // 防止主线程退出
//        try { Thread.sleep(5000); }
//        catch (InterruptedException e) { e.printStackTrace(); }


        // 整个返回
//        String s = deepSeekUtil.askDeepSeek("你好，你能做什么");
//        System.out.println(s);

    }

    @Test
    public void test2() {
        System.out.println(DigestUtils.sha256Hex((SALT + "userPassword").getBytes()));
    }

    @Test
    public void test3() {

        Date date1 = new DateTime("2023-02-02");
        Date date2 = new DateTime("2024-02-02");
        System.out.println(isWithinOneYear(date1, date2));
    }

    public boolean isWithinOneYear(Date date1, Date date2) {
        // 确保date1是较早的日期=

        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        cal.add(Calendar.YEAR, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date oneYearLater = cal.getTime();

        // 判断laterDate是否小于等于一年后的时间
        return !date2.after(oneYearLater);
    }

    @Autowired
    private UserMapper userMapper;

    @Test
    @Transactional
    public void test4() {
        System.out.println("Transaction started");
        try {
            getUser();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void getUser() {
        try {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getId, 1L);
            User user = userMapper.selectOne(queryWrapper);

            user.setUnionId(null);

            updateUser(user);

            int i = 1 / 0;
        }catch (Exception e){
            log.error("Error occurred", e);
            throw new RuntimeException(e);
        }
    }

    public void updateUser(User user){
        userMapper.updateById(user);
    }


    public static void main(String[] args) {
        List<String> list = new java.util.ArrayList<>(List.of("a", "c", "b", "a", "c", "c"));

//        Iterator<String> iterator = list.iterator();
//        while (iterator.hasNext()) {
//            String item = iterator.next();
//            if (item.equals("c")) {
//                iterator.remove();
//            }
//        }
        System.out.println(list);

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals("c"))
                list.remove(i);
        }

        System.out.println(list);
    }
}
