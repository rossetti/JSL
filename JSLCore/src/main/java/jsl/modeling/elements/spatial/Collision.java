/*
 * Copyright (c) 2018. Manuel D. Rossetti, rossetti@uark.edu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package jsl.modeling.elements.spatial;

/**
 *
 */
public class Collision {

	//  ===========================================
	//      CLASS CONSTANTS
	//  ===========================================

	/** Indicates the basic type of collision
	 *  NONE = no collision
	 */
	public final static int NONE = 0;
	
	/** Indicates the basic type of collision
	 *  MOVING = moving element colliding with another moving element
	 */
	public final static int MOVING = 1;
	
	/** Indicates the basic type of collision
	 *  STATIONARY = moving element colliding with a stationary (non-moveable) element
	 */
	public final static int STATIONARY = 2;

	//  ===========================================
	//      CLASS ATTRIBUTES
	//  ===========================================

	/** The element that was moving and detected the collision
	 */
	protected MoverIfc myMovingElement;
	
	/** The moving element that will collide with myMovingElement
	 */
	protected SpatialElement myCollisionElement;
		
	/** The projected time of the collision
	 */
	protected double myTimeOfCollision = Double.POSITIVE_INFINITY;
	
	/** The position associated with the collision
	 */
	protected CoordinateIfc myCollisionPosition;
		
	/** Holds the basic type of collision
	 */
	protected int myCollisionType = NONE;
	
	/** Used to handle the collision
	 */
	protected CollisionHandlerIfc myCollisionHandler;
	
	/** Creates a Collision which represents
	 *  a collsition in space
	 * 
	 */
	public Collision(MoverIfc myMovingElement) {
		setMovingElement(myMovingElement);
	}

	/** Clears the collision for reuse
	 */
	public void clear(){
		myCollisionPosition = null;
		myTimeOfCollision = Double.POSITIVE_INFINITY;
		myCollisionElement = null;
		myCollisionHandler = null;
		myCollisionType  = NONE;
	}
	
	/** Indicates whether or not the elements have collided
	 * @return true if collided
	 */
	public final boolean hasCollided(){
		return(!(myCollisionType == NONE));
	}

	/** Used to indicate the type of collision using one of the class constants
	 * @return Returns the collisionType.
	 */
	public final int getCollisionType() {
		return myCollisionType;
	}
	
	/** The position associated with the collision
	 * @return Returns the collisionPosition.
	 */
	public final CoordinateIfc getCollisionPosition() {
		return myCollisionPosition;
	}
		
	/** The element that will collide with the movingElement
	 * @return Returns the collisionElement.
	 */
	public final SpatialElement getCollisionElement() {
		return myCollisionElement;
	}
		
	/** The element that was moving and detected the collision
	 * @return Returns the movingElement.
	 */
	public final MoverIfc getMovingElement() {
		return myMovingElement;
	}
				
	/** The projected time of the collision
	 * @return Returns the timeOfCollision.
	 */
	public final double getTimeOfCollision() {
		return myTimeOfCollision;
	}

	/** Gets the collision handler for this collision.  It may 
	 *  be null indicating no collision handling actions
	 *  
	 * @return Returns the collisionHandler.
	 */
	public final CollisionHandlerIfc getCollisionHandler() {
		return myCollisionHandler;
	}
	
	/** Sets the characteristics of the collision
	 * 
     * @param element
	 * @param timeOfCollision
	 * @param collisionPosition
	 * @param collisionType The type of collision (moving to moving, moving to stationary)
	 */
	public void setCollision(SpatialElement element,
			double timeOfCollision, CoordinateIfc collisionPosition, int collisionType){

		if (timeOfCollision < 0.0)
			throw new IllegalArgumentException("The time was less than zero.");

		myTimeOfCollision = timeOfCollision;
		
		if (collisionPosition == null)
			throw new IllegalArgumentException("The collisionPosition was null");
		
		myCollisionPosition = collisionPosition;
		
		if (element == null)
			throw new IllegalArgumentException("The colliding element was null");

		myCollisionElement = element;
		
		myCollisionType  = collisionType;
	}
	
	/** Sets the collision handler for this collision.  It may 
	 *  be null indicating no collision handling
	 * @param collisionHandler The collisionHandler to set.
	 */
	public final void setCollisionHandler(CollisionHandlerIfc collisionHandler) {
		myCollisionHandler = collisionHandler;
	}
	
	/**
	 * @param movingElement The movingElement to set.
	 */
	protected final void setMovingElement(MoverIfc movingElement) {
		if (movingElement == null)
			throw new IllegalArgumentException("The moving element was null");
		myMovingElement = movingElement;
	}
}
