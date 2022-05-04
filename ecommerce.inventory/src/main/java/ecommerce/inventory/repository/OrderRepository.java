package ecommerce.inventory.repository;

import org.springframework.data.repository.CrudRepository;

import ecommerce.inventory.dto.Order;

public interface OrderRepository extends CrudRepository<Order, String> {

}
