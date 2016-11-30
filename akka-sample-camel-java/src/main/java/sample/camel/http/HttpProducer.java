package sample.camel.http;

import akka.actor.ActorRef;
import akka.camel.CamelMessage;
import akka.camel.javaapi.UntypedProducerActor;
import org.apache.camel.Exchange;

import java.util.HashSet;
import java.util.Set;

public class HttpProducer extends UntypedProducerActor {
  private ActorRef transformer;

  public HttpProducer(ActorRef transformer) {
    this.transformer = transformer;
  }

  public String getEndpointUri() {
    // bridgeEndpoint=true makes the producer ignore the Exchange.HTTP_URI header, 
    // and use the endpoint's URI for request
    return "jetty://http://akka.io/?bridgeEndpoint=true";
  }

  // before producing messages to endpoints, producer actors can pre-process
  // them by overriding the onTransformOutgoingMessage method
  @Override
  public Object onTransformOutgoingMessage(Object message) {
    if (message instanceof CamelMessage) {
      CamelMessage camelMessage = (CamelMessage) message;
      Set<String> httpPath = new HashSet<String>();
      httpPath.add(Exchange.HTTP_PATH);
      return camelMessage.withHeaders(camelMessage.getHeaders(httpPath));
    } else
      return super.onTransformOutgoingMessage(message);
  }

  // instead of replying to the initial sender, producer actors can implement custom
  // response processing by overriding the onRouteResponse method
  @Override
  public void onRouteResponse(Object message) {
    transformer.forward(message, getContext());
  }
}
