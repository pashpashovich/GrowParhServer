package by.bsuir.growpathserver;

import org.springframework.boot.SpringApplication;

public class TestGrowPathServerApplication {

    public static void main(String[] args) {
        SpringApplication.from(GrowPathServerApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
