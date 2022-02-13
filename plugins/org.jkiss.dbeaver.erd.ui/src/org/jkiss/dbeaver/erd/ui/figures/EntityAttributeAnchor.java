/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2022 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.erd.ui.figures;

import org.eclipse.draw2dl.ChopboxAnchor;
import org.eclipse.draw2dl.geometry.Point;
import org.eclipse.draw2dl.geometry.Rectangle;

public class EntityAttributeAnchor extends ChopboxAnchor {

    private final AttributeItemFigure attrFigure;
    private final boolean isSource;

	public EntityAttributeAnchor(EntityFigure owner, AttributeItemFigure attrFigure, boolean isSource) {
		super(owner);
        this.attrFigure = attrFigure;
        this.isSource = isSource;
	}

	public Point getLocation(Point reference) {
		Rectangle r = Rectangle.SINGLETON;
		r.setBounds(getBox());
		r.translate(-1, -1);
		r.resize(1, 1);

		getOwner().translateToAbsolute(r);
		float centerX = r.x + 0.5f * r.width;
		float centerY = r.y + 0.5f * r.height;

		if (r.isEmpty()
				|| (reference.x == (int) centerX && reference.y == (int) centerY))
			return new Point((int) centerX, (int) centerY); // This avoids
															// divide-by-zero

		float dx = reference.x - centerX;
		float dy = reference.y - centerY;

		// r.width, r.height, dx, and dy are guaranteed to be non-zero.
		float scale = 0.5f / Math.max(Math.abs(dx) / r.width, Math.abs(dy)
				/ r.height);

		dx *= scale;
		dy *= scale;
		centerX += dx;
		centerY += dy;

		return new Point(Math.round(centerX), Math.round(centerY));
	}

	/**
	 * Returns the bounds of this EntityAttributeAnchor's owner. Subclasses can override
	 * this method to adjust the box the anchor can be placed on. For instance,
	 * the owner figure may have a drop shadow that should not be included in
	 * the box.
	 * 
	 * @return The bounds of this EntityAttributeAnchor's owner
	 * @since 2.0
	 */
	protected Rectangle getBox() {
		return attrFigure.getBounds();
	}

	/**
	 * Returns the anchor's reference point. In the case of the EntityAttributeAnchor,
	 * this is the center of the anchor's owner.
	 * 
	 * @return The reference point
	 */
	public Point getReferencePoint() {
	    Point ref = getOwner().getBounds().getCenter();
        ref.y = attrFigure.getLocation().y;
        //attrFigure.translateToAbsolute(entityPoint);
		//Point ref = isSource ? attrFigure.getBounds().getLeft() : attrFigure.getBounds().getRight();
        //Point ref = attrFigure.getBounds().getCenter();
        attrFigure.translateToAbsolute(ref);
		return ref;
	}

	public boolean equals(Object obj) {
		if (obj instanceof EntityAttributeAnchor) {
			return super.equals(obj) &&
                attrFigure == ((EntityAttributeAnchor) obj).attrFigure;
		}
		return false;
	}

	/**
	 * The owning figure's hashcode is used since equality is approximately
	 * based on the owner.
	 * 
	 * @return the hash code.
	 */
	public int hashCode() {
		if (getOwner() != null) {
            return getOwner().hashCode() + attrFigure.hashCode();
        } else {
            return super.hashCode();
        }
	}

}
