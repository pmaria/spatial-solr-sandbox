/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.shape;

import com.spatial4j.core.context.SpatialContext;

import java.util.Collection;

/**
 * A collection of Shape objects.
 */
public class MultiShape implements IShape {
  private final Collection<IShape> geoms;
  private final IRectangle bbox;

  public MultiShape(Collection<IShape> geoms, SpatialContext ctx) {
    this.geoms = geoms;
    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE;
    double maxY = Double.MIN_VALUE;
    for (IShape geom : geoms) {
      IRectangle r = geom.getBoundingBox();
      minX = Math.min(minX,r.getMinX());
      minY = Math.min(minY,r.getMinY());
      maxX = Math.max(maxX,r.getMaxX());
      maxY = Math.max(maxY,r.getMaxY());
    }
    this.bbox = ctx.makeRect(minX, maxX, minY, maxY);
  }

  @Override
  public IRectangle getBoundingBox() {
    return bbox;
  }

  @Override
  public IPoint getCenter() {
    return bbox.getCenter();
  }

  @Override
  public boolean hasArea() {
    for (IShape geom : geoms) {
      if( geom.hasArea() ) {
        return true;
      }
    }
    return false;
  }

  @Override
  public SpatialRelation relate(IShape other, SpatialContext ctx) {
    boolean allOutside = true;
    boolean allContains = true;
    for (IShape geom : geoms) {
      SpatialRelation sect = geom.relate(other, ctx);
      if (sect != SpatialRelation.DISJOINT)
        allOutside = false;
      if (sect != SpatialRelation.CONTAINS)
        allContains = false;
      if (!allContains && !allOutside)
        return SpatialRelation.INTERSECTS;//short circuit
    }
    if (allOutside)
      return SpatialRelation.DISJOINT;
    if (allContains)
      return SpatialRelation.CONTAINS;
    return SpatialRelation.INTERSECTS;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MultiShape that = (MultiShape) o;

    if (geoms != null ? !geoms.equals(that.geoms) : that.geoms != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return geoms != null ? geoms.hashCode() : 0;
  }
}
