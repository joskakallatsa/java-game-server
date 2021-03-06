package org.menacheri.protocols.impl;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.base64.Base64Decoder;
import org.jboss.netty.handler.codec.base64.Base64Encoder;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.menacheri.app.IPlayerSession;
import org.menacheri.handlers.netty.AMF3ToJavaObjectDecoder;
import org.menacheri.handlers.netty.JavaObjectToAMF3Encoder;
import org.menacheri.handlers.netty.NulEncoder;
import org.menacheri.protocols.AbstractNettyProtocol;
import org.menacheri.util.NettyUtils;


/**
 * This protocol defines AMF3 that is base 64 and String encoded sent over the
 * wire. Used by XMLSocket flash clients to send AMF3 data.
 * 
 * @author Abraham Menacherry
 * 
 */
public class AMF3StringProtocol extends AbstractNettyProtocol
{
	/**
	 * The maximum size of the incoming message in bytes. The
	 * {@link DelimiterBasedFrameDecoder} will use this value in order to throw
	 * a {@link TooLongFrameException}.
	 */
	int maxFrameSize;
	/**
	 * The flash client would encode the AMF3 bytes into a base 64 encoded
	 * string, this decoder is used to decode it back.
	 */
	private Base64Decoder base64Decoder;
	/**
	 * This decoder will do the actual serialization to java object. Any game
	 * handlers need to be added after this in the pipeline so that they can
	 * operate on the java object.
	 */
	private AMF3ToJavaObjectDecoder amf3ToJavaObjectDecoder;
	/**
	 * Once the game handler is done with its operations, it writes back the
	 * java object to the client. When writing back to flash client, it needs to
	 * use this encoder to encode it to AMF3 format.
	 */
	private JavaObjectToAMF3Encoder javaObjectToAMF3Encoder;
	/**
	 * The flash client expects a AMF3 bytes to be passed in as base 64 encoded
	 * string. This encoder will encode the bytes accordingly.
	 */
	private Base64Encoder base64Encoder;
	/**
	 * Flash client expects a nul byte 0x00 to be added as the end byte of any
	 * communication with it. This encoder will add this nul byte to the end of
	 * the message. Could be considered as a message "footer".
	 */
	private NulEncoder nulEncoder;

	public AMF3StringProtocol()
	{
		super("AMF3_STRING");
	}

	@Override
	public void applyProtocol(IPlayerSession playerSession)
	{
		ChannelPipeline pipeline = NettyUtils
				.getPipeLineOfConnection(playerSession);

		// Upstream handlers or encoders (i.e towards server) are added to
		// pipeline now.
		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(maxFrameSize,
				Delimiters.nulDelimiter()));
		pipeline.addLast("base64Decoder", base64Decoder);
		pipeline.addLast("amf3ToJavaObjectDecoder", amf3ToJavaObjectDecoder);

		// Downstream handlers - Filter for data which flows from server to
		// client. Note that the last handler added is actually the first
		// handler for outgoing data.
		pipeline.addLast("nulEncoder", nulEncoder);
		pipeline.addLast("base64Encoder", base64Encoder);
		pipeline.addLast("javaObjectToAMF3Encoder", javaObjectToAMF3Encoder);
	}

	public int getMaxFrameSize()
	{
		return maxFrameSize;
	}

	public void setMaxFrameSize(int frameSize)
	{
		this.maxFrameSize = frameSize;
	}

	public Base64Decoder getBase64Decoder()
	{
		return base64Decoder;
	}

	public void setBase64Decoder(Base64Decoder base64Decoder)
	{
		this.base64Decoder = base64Decoder;
	}

	public AMF3ToJavaObjectDecoder getAmf3ToJavaObjectDecoder()
	{
		return amf3ToJavaObjectDecoder;
	}

	public void setAmf3ToJavaObjectDecoder(
			AMF3ToJavaObjectDecoder amf3ToJavaObjectDecoder)
	{
		this.amf3ToJavaObjectDecoder = amf3ToJavaObjectDecoder;
	}

	public JavaObjectToAMF3Encoder getJavaObjectToAMF3Encoder()
	{
		return javaObjectToAMF3Encoder;
	}

	public void setJavaObjectToAMF3Encoder(
			JavaObjectToAMF3Encoder javaObjectToAMF3Encoder)
	{
		this.javaObjectToAMF3Encoder = javaObjectToAMF3Encoder;
	}

	public Base64Encoder getBase64Encoder()
	{
		return base64Encoder;
	}

	public void setBase64Encoder(Base64Encoder base64Encoder)
	{
		this.base64Encoder = base64Encoder;
	}

	public NulEncoder getNulEncoder()
	{
		return nulEncoder;
	}

	public void setNulEncoder(NulEncoder nulEncoder)
	{
		this.nulEncoder = nulEncoder;
	}

}
