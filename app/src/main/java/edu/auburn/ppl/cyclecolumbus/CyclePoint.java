/**	 Cycle Altanta, Copyright 2012 Georgia Institute of Technology
 *                                    Atlanta, GA. USA
 *
 *   @author Christopher Le Dantec <ledantec@gatech.edu>
 *   @author Anhong Guo <guoanhong15@gmail.com>
 *
 *   Updated/Modified for Atlanta's app deployment. Based on the
 *   CycleTracks codebase for SFCTA.
 *
 *   CycleTracks, Copyright 2009,2010 San Francisco County Transportation Authority
 *                                    San Francisco, CA, USA
 *
 * 	 @author Billy Charlton <billy.charlton@sfcta.org>
 *
 *   This file is part of CycleTracks.
 *
 *   CycleTracks is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CycleTracks is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CycleTracks.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.auburn.ppl.cyclecolumbus;

class CyclePoint {
	public float accuracy;
	public double altitude;
	public float speed;
	public double time;
	int latitude;
	int longitude;

	public CyclePoint(int lat, int lgt, double currentTime) {
		this.latitude = lat;
		this.longitude = lgt;
		this.time = currentTime;
	}

	public CyclePoint(int lat, int lgt, double currentTime, float accuracy) {
		this.latitude = lat;
		this.longitude = lgt;
		this.time = currentTime;
		this.accuracy = accuracy;
	}

	public CyclePoint(int lat, int lgt, double currentTime, float accuracy,
			double altitude, float speed) {
		this.latitude = lat;
		this.longitude = lgt;
		this.time = currentTime;
		this.accuracy = accuracy;
		this.altitude = altitude;
		this.speed = speed;
	}
}
