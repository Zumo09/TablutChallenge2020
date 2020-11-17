package giordani.tabzai.player.brain;

public class TimeOutException extends Exception {
	private static final long serialVersionUID = 1L;

	public TimeOutException() {
	}

	public TimeOutException(String message) {
		super(message);
	}

	public TimeOutException(Throwable cause) {
		super(cause);
	}

	public TimeOutException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimeOutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
