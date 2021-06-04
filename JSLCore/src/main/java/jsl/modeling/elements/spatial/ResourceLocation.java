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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jsl.simulation.ModelElement;


/** A ResourceLocation holds a set of resources that are assigned to this location
 *  in a spatial model.  A ResourceLocation serves a "home base" for the SpatialResource.
 *  
 *  Clients can use the ResourceLocation to get idle resources that have been assigned to that location
 *  Since a ResourceLocation is a SpatialModelElement it is represented in a spatial model and can thus
 *  be visited by clients in search of resources that are assigned to the location.
 *  
 *  SpatialResource are assigned to the location, but that does not mean that the SpatialResource is currently
 *  physically located at the ResourceLocation.
 *  
 *
 */
public class ResourceLocation extends SpatialModelElement {

	/** Holds the resoures assigned to this location
	 * 
	 */
	protected Set<SpatialResource> myResources;

	/** Creates a ResourceLocation with (0.0, 0.0) position.  
	 *  The SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
	 *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
	 * 
	 * @param parent
	 */
	public ResourceLocation(ModelElement parent){
		this (parent, null, null, null);
	}
	
	/** Creates a ResourceLocation with (0.0, 0.0) position.  
	 *  The SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
	 *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
	 * 
	 * @param parent
	 * @param name
	 */
	public ResourceLocation(ModelElement parent, String name){
		this (parent, name, null, null);
	}

	/** Creates a ResourceLocation with the given  (x,y) position.  
	 *  The SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
	 *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
	 * 
	 * @param parent
	 * @param name
	 * @param x
	 * @param y
	 */
	public ResourceLocation(ModelElement parent, String name, double x, double y){
		this (parent, name, null, x, y, 0.0);
	}

	/** Creates a ResourceLocation with the given  (x,y) position.  
	 *  The SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
	 *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
	 * 
	 * @param parent
	 * @param x
	 * @param y
	 */
	public ResourceLocation(ModelElement parent, double x, double y){
		this (parent, null, null, x, y, 0.0);
	}

	/** Creates a ResourceLocation with at the coordinates of the supplied position.  
	 *  The SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
	 *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
	 * 
	 * @param parent
	 * @param name
	 * @param position
	 */
	public ResourceLocation(ModelElement parent, String name, CoordinateIfc position){
		this (parent, name, null, position);
	}
	
	/** Creates a ResourceLocation with at the coordinates of the supplied position.  
	 *  The SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
	 *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
	 * 
	 * @param parent
	 * @param position
	 */
	public ResourceLocation(ModelElement parent, CoordinateIfc position){
		this (parent, null, null, position);
	}
	
	/** Creates a ResourceLocation with the given parent and SpatialModel2D.  The default position
	 *  is (0.0, 0.0).  If the SpatialModel2D
	 *  is null, the SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
	 *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
	 * 
	 * @param parent
	 * @param name
	 * @param spatialModel
	 */
	public ResourceLocation(ModelElement parent, String name, SpatialModel spatialModel){
		this (parent, name, spatialModel, null);
	}

	/** Creates a ResourceLocation with the given parent and SpatialModel2D.  The default position
	 *  is (0.0, 0.0).  If the SpatialModel2D
	 *  is null, the SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
	 *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
	 * 
	 * @param parent
	 * @param spatialModel
	 */
	public ResourceLocation(ModelElement parent, SpatialModel spatialModel){
		this (parent, null, spatialModel, null);
	}

	/** Creates a ResourceLocation with the given parent and SpatialModel2D.  If the SpatialModel2D
	 *  is null, the SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
	 *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
	 * 
	 * @param parent
	 * @param name
	 * @param spatialModel
	 * @param x
	 * @param y
	 * @param z
	 */
	public ResourceLocation(ModelElement parent, String name, SpatialModel spatialModel, double x, double y, double z){
		this (parent, null, null, new Vector3D(x,y,z));
	}
	
	/** Creates a ResourceLocation with the given parent and SpatialModel2D.  If the SpatialModel2D
	 *  is null, the SpatialModel2D of the parent is used as the SpatialModel2D.  If the parent
	 *  does not have a SpatialModel2D, then an IllegalArgumentException is thrown
	 * 
	 * @param parent
	 * @param name
	 * @param spatialModel
	 * @param coordinate
	 */
	public ResourceLocation(ModelElement parent, String name, SpatialModel spatialModel, CoordinateIfc coordinate){
		super(parent, name, spatialModel, coordinate);
		
		myResources = new LinkedHashSet<SpatialResource>();		
	}
	
	/** Adds the supplied resource to this location and sets the initial
	 *  position of the resource to this coordinates. Only sets the initial 
	 *  position if the add was successful.
	 *  
	 *  A SpatialResource can only be assigned to one location at a time.  An attempt
	 *  to add a SpatialResource to a location when it is already assigned to another
	 *  location will cause an exception.  First check the getResourceLocation() method
	 *  on SpatialResource to see if it is assigned a location. If so, remove it from that
	 *  location prior to assigning it to this location.
	 * 
	 * @param resource
	 * @return
	 */
	public boolean addInitialResource(SpatialResource resource){
		if (resource.getResourceLocation() != null)
			throw new IllegalArgumentException("The Resource2D has already been assigned a ResourceLocation. First remove it from its current location");
			
		boolean b = myResources.add(resource);
		if (b == true) {
			resource.setInitialResourceLocation(this);
			resource.setInitialPosition(getPosition());
		}
		return(b);
	}

	/** Adds the supplied resource to this location and sets
	 *  the CURRENT position of the resource to this location. Only sets the
	 *  position if the add was successful

	 *  A SpatialResource can only be assigned to one location at a time.  An attempt
	 *  to assign a SpatialResource to a location when it is already assigned to another
	 *  location will cause an exception.  First check the getResourceLocation() method
	 *  on SpatialResource to see if it is assigned a location. If so, remove it from that
	 *  location prior to assigning it to this location.
	 * 
	 * @param resource
	 * @return
	 */
	public boolean add(SpatialResource resource) {
		if (resource.getResourceLocation() != null)
			throw new IllegalArgumentException("The Resource2D has already been assigned a ResourceLocation. First remove it from its current location");

		boolean b = myResources.add(resource);
		if (b == true){
			resource.setResourceLocation(this);
			resource.setPosition(getPosition());
		}
		return(b);
	}

	/** Checks if the supplied resource is assigned to this location
	 * 
	 * @param resource
	 * @return
	 */
	public boolean contains(SpatialResource resource) {
		return myResources.contains(resource);
	}

	/** Indicates whether or not this location has resources assigned to it
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return myResources.isEmpty();
	}

	/** Returns an iterator to the resources assigned to this location
	 * 
	 * @return
	 */
	public Iterator<SpatialResource> iterator() {
		return myResources.iterator();
	}

	/** Removes the supplied resource from this location.  The resource
	 *  is no longer assigned to this location
	 * 
	 * @param resource
	 * @return Returns true if the remove was successful
	 */
	public boolean remove(SpatialResource resource) {
		boolean b = myResources.remove(resource);
		if (b) resource.setResourceLocation(null);
		return (b);
	}

	/** Returns the number of resources assigned to this location
	 * 
	 * @return
	 */
	public int getNumberOfResources() {
		return myResources.size();
	}
	
	/** Returns the number of idle resources currently assigned to this location
	 * 
	 * @return
	 */
	public int getNumberOfIdleResources(){
		
		int i = 0;
		for(SpatialResource r: myResources){
			if (r.isIdle())
				i++;
		}
		return(i);
	}
	
	/** This finds an idle resource assigned to this location.  Returns and idle resource
	 *  or null if none found.  This method does not remove the resource
	 *  from this location.
	 * 
	 * @return
	 */
	public SpatialResource findIdleResource2D(){
		
		SpatialResource resource = null;
		
		for(SpatialResource r: myResources){
			if (r.isIdle()){
				resource = r;
				break;
			}
		}
		return(resource);
	}
	
	/** Returns a list of the idle resources assigned to this location
	 * 
	 * @return
	 */
	public List<SpatialResource> findIdleResources2D(){
		
		if (myResources.isEmpty())
			return(null);
		
		List<SpatialResource> idleResources = null;
		
		for(SpatialResource r: myResources){
			if (r.isIdle()){
				if (idleResources == null)
					idleResources = new ArrayList<SpatialResource>();
				if (idleResources != null)
					idleResources.add(r);
			}
		}
		return(idleResources);		
	}

}
