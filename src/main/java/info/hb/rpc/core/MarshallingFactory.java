package info.hb.rpc.core;

import io.netty.handler.codec.marshalling.DefaultMarshallerProvider;
import io.netty.handler.codec.marshalling.DefaultUnmarshallerProvider;
import io.netty.handler.codec.marshalling.MarshallerProvider;
import io.netty.handler.codec.marshalling.MarshallingDecoder;
import io.netty.handler.codec.marshalling.MarshallingEncoder;
import io.netty.handler.codec.marshalling.UnmarshallerProvider;

import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;

public class MarshallingFactory {

	public static MarshallingDecoder createDecoder() {
		MarshallerFactory factory = Marshalling.getProvidedMarshallerFactory("serial");
		MarshallingConfiguration config = new MarshallingConfiguration();
		config.setVersion(5);
		UnmarshallerProvider provider = new DefaultUnmarshallerProvider(factory, config);
		return new MarshallingDecoder(provider, 10240);
	}

	public static MarshallingEncoder createEncoder() {
		MarshallerFactory factory = Marshalling.getProvidedMarshallerFactory("serial");
		MarshallingConfiguration config = new MarshallingConfiguration();
		config.setVersion(5);
		MarshallerProvider provider = new DefaultMarshallerProvider(factory, config);
		return new MarshallingEncoder(provider);
	}

}
