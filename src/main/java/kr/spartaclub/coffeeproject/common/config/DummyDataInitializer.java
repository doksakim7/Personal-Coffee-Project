package kr.spartaclub.coffeeproject.common.config;

import kr.spartaclub.coffeeproject.common.enums.MenuStatus;
import kr.spartaclub.coffeeproject.common.enums.MenuType;
import kr.spartaclub.coffeeproject.domain.menu.entity.Menu;
import kr.spartaclub.coffeeproject.domain.menu.repository.MenuRepository;
import kr.spartaclub.coffeeproject.domain.user.entity.User;
import kr.spartaclub.coffeeproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Slf4j
@Configuration
@Profile("local")
@RequiredArgsConstructor
public class DummyDataInitializer {

    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDummyData() {
        return args -> {
            log.info("더미데이터 생성을 시작합니다.");
            initUsers();
            initMenus();
            log.info("더미데이터 생성이 완료되었습니다.");
        };
    }

    // ===== 유저 생성 =====
    private void initUsers() {
        if (userRepository.existsByEmail("test@test.com")) {
            log.info("테스트 유저가 이미 존재하여 생성하지 않습니다.");
            return;
        }

        User user = new User(
                "test@test.com",
                passwordEncoder.encode("abcd1234!")
        );

        userRepository.save(user);
        log.info("테스트 유저 생성 완료");
    }

    // ===== 메뉴 생성 =====
    private void initMenus() {
        if (menuRepository.count() > 0) {
            log.info("메뉴 더미데이터가 이미 존재하여 생성하지 않습니다.");
            return;
        }

        List<Menu> menus = List.of(
                new Menu("아메리카노", 4000L, MenuType.COFFEE, MenuStatus.AVAILABLE, null),
                new Menu("카페라떼", 4500L, MenuType.LATTE, MenuStatus.AVAILABLE, null),
                new Menu("바닐라라떼", 5000L, MenuType.LATTE, MenuStatus.AVAILABLE, null),
                new Menu("레몬에이드", 5500L, MenuType.ADE, MenuStatus.AVAILABLE, null),
                new Menu("자몽에이드", 5500L, MenuType.ADE, MenuStatus.SOLD_OUT, null),
                new Menu("얼그레이", 4800L, MenuType.TEA, MenuStatus.AVAILABLE, null),
                new Menu("녹차", 4500L, MenuType.TEA, MenuStatus.INACTIVE, null),
                new Menu("치즈케이크", 6000L, MenuType.DESSERT, MenuStatus.AVAILABLE, null),
                new Menu("초코케이크", 6500L, MenuType.DESSERT, MenuStatus.SOLD_OUT, null),
                new Menu("브런치세트", 12000L, MenuType.SET, MenuStatus.INACTIVE, null)
        );

        menuRepository.saveAll(menus);
        log.info("메뉴 더미데이터 생성 완료");
    }

}
