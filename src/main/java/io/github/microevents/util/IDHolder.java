package io.github.microevents.util;

/**
 * a table to determine if a number has already been used, effectively a super memory compact, and fast IntSet
 * although it's memory compact, it's still a hog if u want huge numbers because all the storage space is allocated
 * up front
 */
public class IDHolder {
	/**
	 * used for storing all the possible ids
	 */
	private long[] values;

	/**
	 * last checked index, for faster sequential access
	 */
	private int last = 0;

	/**
	 * creates a new IDHolder that can accept the specified amount of entries
	 *
	 * @param capacity rounded up to the nearest 64
	 */
	public IDHolder(int capacity) {
		values = new long[(capacity - 1 >> 6) + 1];
	}

	/**
	 * checks whether the specified id is already occupied
	 *
	 * @param val the id
	 * @return true if the value is occupied
	 * @throws ArrayIndexOutOfBoundsException if value is outside of specified range
	 */
	public boolean occupied(int val) {
		return (values[val >>> 6] & 1L << (val & 63)) != 0;
	}

	/**
	 * gets the first unoccupied id and occupied it
	 *
	 * @return the smallest unoccupied id
	 * @throws IndexOutOfBoundsException if the capacity has been exceeded
	 */
	public int next() {
		int val = occupyBit(last);
		if (val != -1) return val + (last << 6);

		for (int i = 0; i < values.length; i++)
			if (values[i] != 0xFFFFFFFFFFFFFFFFL) {
				last = i;
				return (i << 6) + occupyBit(i);
			}
		throw new IndexOutOfBoundsException("Capacity Exceed");
	}

	/**
	 * checks if the id holder is full
	 * @return true if capacity has been reached
	 */
	public boolean full() {
		return peek() == -1;
	}

	/**
	 * same as {@link IDHolder#next()} but does not occupied the id and doesn't throw an out of bounds exception if capacity
	 * was exceeded
 	 * @return the smallest unoccupied id or -1 if capacity has been reached
	 */
	public int peek() {
		for (int i = 0; i < values.length; i++)
			if (values[i] != 0xFFFFFFFFFFFFFFFFL) return (i << 6) + peekBit(i);
		return -1;
	}

	/**
	 * removes the id from the holder
	 * @param val the value to be removed
	 * @throws ArrayIndexOutOfBoundsException if the value is larger than the capacity
	 */
	public void remove(int val) {
		last = val >>> 6;
		values[last] ^= 1L << (val & 63);
	}


	private int occupyBit(int index) {
		for (int x = 0; x < 64; x++)
			if ((values[index] & (1L << x)) == 0) {
				values[index] |= 1L << x;
				return x;
			}
		return -1;
	}

	private int peekBit(int index) {
		for (int x = 0; x < 64; x++)
			if ((values[index] & (1L << x)) == 0) return x;
		return -1;
	}

}
