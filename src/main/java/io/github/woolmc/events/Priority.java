package io.github.woolmc.events;

public enum Priority {

	/**
	 * all cancel calls are ignored here, but are respected later on
	 */
	TOP,

	/**
	 * executed first
	 */
	LOWEST,

	/**
	 * executed after {@link Priority#LOWEST}
	 */
	LOW,

	/**
	 * executed after {@link Priority#LOW}
	 */
	NORMAL,

	/**
	 * executed after {@link Priority#NORMAL}
	 */
	HIGH,

	/**
	 * executed after {@link Priority#HIGH}
	 */
	HIGHEST,

	/**
	 * executed after {@link Priority#HIGHEST}, should not mutate event with this priority!
	 */
	FINAL;

	private static final Priority[] ORDINALS = values();
	private final int flag; // 1 byte
	Priority() {
		this.flag = ordinal();
	}

	public int sign(int val) {
		return val | flag;
	}

	public static Priority getFromFlag(int val) {
		return ORDINALS[val & 7];
	}
}
