package ecommerce.order.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import ecommerce.order.dto.EventStore;

public interface EventStoreRepository extends CrudRepository<EventStore, Long> {
	
	@Query(value = "SELECT UUID()", nativeQuery = true)
	public String getNewOrderId();

}
