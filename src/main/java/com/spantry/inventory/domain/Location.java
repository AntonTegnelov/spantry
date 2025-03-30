package com.spantry.inventory.domain;

import java.io.Serializable;

/** Represents potential locations for storing items. */
public enum Location implements Serializable {
  PANTRY,
  FRIDGE,
  FREEZER,
  CUPBOARD,
  COUNTER, // For items not requiring specific storage conditions
  OTHER // For less common locations
}
