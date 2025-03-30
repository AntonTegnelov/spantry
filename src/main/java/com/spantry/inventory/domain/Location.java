package com.spantry.inventory.domain;

/** Represents potential locations for storing items. */
public enum Location {
  PANTRY,
  FRIDGE,
  FREEZER,
  CUPBOARD,
  COUNTER, // For items not requiring specific storage conditions
  OTHER // For less common locations
}
