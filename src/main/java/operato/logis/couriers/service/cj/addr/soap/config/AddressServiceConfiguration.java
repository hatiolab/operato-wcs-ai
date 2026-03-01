package operato.logis.couriers.service.cj.addr.soap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import operato.logis.couriers.service.cj.addr.soap.client.AddressServiceClient;

@Configuration
public class AddressServiceConfiguration {

	@Bean
	public Jaxb2Marshaller marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setContextPath("operato.logis.couriers.service.cj.addr.soap.wsdl");
		return marshaller;
	}

	@Bean
	public AddressServiceClient addressServiceClient(Jaxb2Marshaller marshaller) {
		AddressServiceClient client = new AddressServiceClient();
		client.setDefaultUri("http://webservice.address.nplus.doortodoor.co.kr/");
		client.setMarshaller(marshaller);
		client.setUnmarshaller(marshaller);
		return client;
	}
}
