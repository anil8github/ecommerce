package ecommerce.inventory.repository;

import org.springframework.data.repository.CrudRepository;

import ecommerce.inventory.dto.EventStore;

public interface EventStoreRepository extends CrudRepository<EventStore, Long> {

}
