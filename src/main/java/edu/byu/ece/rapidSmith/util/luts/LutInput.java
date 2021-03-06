/*
 * Copyright (c) 2016 Brigham Young University
 *
 * This file is part of the BYU RapidSmith Tools.
 *
 * BYU RapidSmith Tools is free software: you may redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * BYU RapidSmith Tools is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * A copy of the GNU General Public License is included with the BYU
 * RapidSmith Tools. It can be found at doc/LICENSE.GPL3.TXT. You may
 * also get a copy of the license at <http://www.gnu.org/licenses/>.
 */

package edu.byu.ece.rapidSmith.util.luts;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
* A LUT input, possibly inverted, in a {@link LutEquation}.  The index is the equivalent
 * index of the LUT's input (typically 1-6).
*/
public final class LutInput extends LutEquation {
	private int index;
	private boolean inverted;

	/**
	 * Constructs a new LutInput for the input pin with the specified index.
	 *
	 * @param index index of the corresponding input pin
	 */
	public LutInput(int index) {
		this(index, false);
	}

	/**
	 * Constructs a new LutInput for the input pin with the specified index.
	 *
	 * @param index index of the corresponding input pin
	 * @param inverted true if the pin should be inverted
	 */
	public LutInput(int index, boolean inverted) {
		setIndex(index);
		setInverted(inverted);
	}

	/**
	 * @return the index of this pin (usually 1-6)
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Sets the index of this pin.
	 *
	 * @param index index to update the LutInput to
	 * @throws IllegalArgumentException if the index is less than 1
	 */
	public void setIndex(int index) {
		if (index < 1)
			throw new IllegalArgumentException("LUT indices start at 1");

		this.index = index;
	}

	/**
	 * @return true if this input is inverted in the equation
	 */
	public boolean isInverted() {
		return inverted;
	}

	/**
	 * Sets whether the input should be inverted or not.
	 *
	 * @param inverted true if the input should be inverted
	 */
	public void setInverted(boolean inverted) {
		this.inverted = inverted;
	}

	@Override
	public String toString() {
		return (inverted ? "~" : "") + "A" + index;
	}

	@Override
	public LutEquation deepCopy() {
		return new LutInput(index, inverted);
	}

	@Override
	public void remapPins(Map<Integer, Integer> mapping) {
		Integer newIndex = mapping.get(index);
		if (newIndex != null)
			index = newIndex;
	}

	@Override
	protected void getUsedInputs(Set<Integer> usedInputs) {
		usedInputs.add(getIndex());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LutInput)) return false;
		LutInput lutInput = (LutInput) o;
		return getIndex() == lutInput.getIndex() &&
				isInverted() == lutInput.isInverted();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getIndex(), isInverted());
	}
}
