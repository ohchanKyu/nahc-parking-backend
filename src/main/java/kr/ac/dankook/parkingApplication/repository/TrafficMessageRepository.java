package kr.ac.dankook.parkingApplication.repository;

import kr.ac.dankook.parkingApplication.document.TrafficMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrafficMessageRepository extends MongoRepository<TrafficMessage, String> {}
