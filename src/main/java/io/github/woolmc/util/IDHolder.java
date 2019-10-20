package io.github.woolmc.util;

/**
 * a table to determine if a number has already been used, effectively a super memory compact IntSet
 */
public class IDHolder {
	/**
	 * used for storing
	 */
	private long[] values;
	/**
	 * last checked index
	 */
	private int last = 0;
	/**
	 * creates a new IDHolder that can accept the specified amount of entries
	 * @param capacity rounded up to the nearest 64
	 */
	public IDHolder(int capacity) {
		values = new long[(capacity-1)/64 + 1];
	}


	public int getNew() {
		int val = occupyBit(last);
		if(val != -1)
			return (last << 6)+val;

		for (int i = 0; i < values.length; i++)
			if (values[i] != 0xFFFFFFFFFFFFFFFFL) {
				last = i;
				return (i << 6) + occupyBit(i);
			}
		throw new IndexOutOfBoundsException("Capacity Exceed");
	}

	public int peek() {
		for (int i = 0; i < values.length; i++)
			if (values[i] != 0xFFFFFFFFFFFFFFFFL)
				return (i << 6) + peekBit(i);
		return -1;
	}

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
			if ((values[index] & (1L << x)) == 0)
				return x;
		return -1;
	}

}
