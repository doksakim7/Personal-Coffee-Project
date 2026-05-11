package kr.spartaclub.coffeeproject.domain.menu.repository;

import kr.spartaclub.coffeeproject.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long>, MenuCustomRepository {

}
