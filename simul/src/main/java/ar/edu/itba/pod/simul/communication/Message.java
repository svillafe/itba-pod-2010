package ar.edu.itba.pod.simul.communication;

import java.io.Serializable;

import ar.edu.itba.pod.simul.communication.payload.Payload;

import com.google.common.base.Preconditions;

/**
 * The <code>Message</code> class represents the messages that can be sent between the nodes of the cluster.
 * <p>
 * A message contains information about the original node that created the message and the local time stamp of the node.
 * <p>
 * This class is not thread safe.
 */
public class Message implements Serializable, Comparable<Message> {

	private final MessageType type;
	private final String nodeId;
	private final Long timeStamp;
	private Payload payload;

	public Message(String nodeId, long timeStamp, MessageType type, Payload payload) {
		super();
		Preconditions.checkNotNull(nodeId, "Message cannot be created with null node");
		Preconditions.checkNotNull(type, "Message cannot be created with null type");
		Preconditions.checkNotNull(payload, "Message cannot be created with null payload");
		this.nodeId = nodeId;
		this.timeStamp = timeStamp;
		this.type = type;
		this.payload = payload;
	}

	/**
	 * @return the payload
	 */
	public Payload getPayload() {
		return payload;
	}

	/**
	 * @return the type
	 */
	public MessageType getType() {
		return type;
	}

	public String getNodeId() {
		return new String(nodeId);
	}

	public Long getTimeStamp() {
		return new Long(timeStamp);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
		result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
		return result;
	}

	/**
	 * Two messages are equals if they are the same object instance, or the values of the original node identification
	 * and time stamp are equals.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		if (timeStamp == null) {
			if (other.timeStamp != null)
				return false;
		} else if (!timeStamp.equals(other.timeStamp))
			return false;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		return true;
	}

	/**
	 * Comparison is implemented first by comparing the time stamp and then by comparing the original node
	 * identification
	 */
	@Override
	public int compareTo(Message other) {
		if (getTimeStamp().compareTo(other.getTimeStamp()) == 0) {
			return getNodeId().compareTo(other.getNodeId());
		} else {
			return getTimeStamp().compareTo(other.getTimeStamp());
		}
	}
}
