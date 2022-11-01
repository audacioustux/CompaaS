import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import java.util.Properties
import scala.util.Using

object ProducerExample {
  private val bootstrapServers = System.getenv("REDPANDA_BROKERS")
  private val topicName        = "greetings"

  def getProducerConfig(): Properties = {
    // Specify the configuration for our producer
    var properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false)

    // Both keys and values in the greetings topic will be serialized as strings
    properties.put(
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
      (new StringSerializer).getClass.getName
    )
    properties.put(
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
      (new StringSerializer).getClass.getName
    )
    return properties;
  }

  def apply(): Unit = {

    val producerConfig = getProducerConfig();

    // Get a Producer instance
    Using(new KafkaProducer[String, String](producerConfig)) { producer =>
      val record = new ProducerRecord(topicName, "key1", "Hello World!")

      producer.send(record)

      producer.flush()
      System.out.printf("Produced record. key=%s, value=%s\n", record.key(), record.value())
    }
  }
}
