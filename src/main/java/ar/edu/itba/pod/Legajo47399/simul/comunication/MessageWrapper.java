package ar.edu.itba.pod.Legajo47399.simul.comunication;

import java.io.Serializable;

import ar.edu.itba.pod.simul.communication.Message;

public class MessageWrapper implements Serializable, Comparable<MessageWrapper> {

	private Message message;
	private Long nodeTimeStamp;

	public MessageWrapper(Message message, Long nodeTimeStamp) {
		super();
		this.message = message;
		this.nodeTimeStamp = nodeTimeStamp;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public Long getNodeTimeStamp() {
		return nodeTimeStamp;
	}

	public void setNodeTimeStamp(Long nodeTimeStamp) {
		this.nodeTimeStamp = nodeTimeStamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result
				+ ((nodeTimeStamp == null) ? 0 : nodeTimeStamp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageWrapper other = (MessageWrapper) obj;
		return this.message.equals(other.message);
	}

	@Override
	public int compareTo(MessageWrapper other) {
		//return this.message.compareTo(other.message);
		if (getNodeTimeStamp().compareTo(other.getNodeTimeStamp()) == 0) {
			return getMessage().compareTo(other.getMessage());
		} else {
			return getNodeTimeStamp().compareTo(other.getNodeTimeStamp());
		}
	}

	@Override
	public String toString() {
		return "MessageWrapper [message=" + message + ", nodeTimeStamp="
				+ nodeTimeStamp + "]";
	}
	
	

}
