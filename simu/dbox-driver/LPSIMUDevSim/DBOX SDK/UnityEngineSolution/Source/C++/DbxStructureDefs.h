#pragma once

#include "LiveMotion/dboxLiveMotion.h"

// The following Structures and Events are examples. They must be modified to 
// include all Structures and Events needed for the experience.

enum AppEvents {
	// FRAME
	FRAME_UPDATE,			// accel, velo, rotation, health, shield

	// CONFIG
	CONFIG_UPDATE,			// weapon 1, weapon 2, equip 1, equip 2, max health, max shield, ID

	// ACTION_WITH_DATA
	ACTION_WITH_DATA,		// type, distance, orientation

	// ACTION
	ACTION
};

struct FrameUpdate {
	dbox::XyzFloat32	Acceleration;
	dbox::XyzFloat32	Velocity;
	dbox::XyzFloat32	Orientation;
	dbox::F32			WeaponOrientation;
	dbox::F32			Health;
	dbox::F32			Shield;

	DBOX_STRUCTINFO_BEGIN()
		DBOX_STRUCTINFO_FIELD(FrameUpdate, Acceleration, dbox::FT_XYZ_FLOAT32, dbox::FM_ACCELERATION_XYZ)
		DBOX_STRUCTINFO_FIELD(FrameUpdate, Velocity, dbox::FT_XYZ_FLOAT32, dbox::FM_VELOCITY_XYZ)
		DBOX_STRUCTINFO_FIELD(FrameUpdate, Orientation, dbox::FT_XYZ_FLOAT32, dbox::FM_EVENT_ORIENTATION_XYZ)
		DBOX_STRUCTINFO_FIELD(FrameUpdate, WeaponOrientation, dbox::FT_FLOAT32, dbox::FM_YAW_RAD)
		DBOX_STRUCTINFO_FIELD(FrameUpdate, Health, dbox::FT_FLOAT32, dbox::FM_FRAME_CUSTOM_01)
		DBOX_STRUCTINFO_FIELD(FrameUpdate, Shield, dbox::FT_FLOAT32, dbox::FM_FRAME_CUSTOM_02)
		DBOX_STRUCTINFO_END()

		FrameUpdate(dbox::XyzFloat32 dAcceleration, dbox::XyzFloat32 dVelocity, dbox::XyzFloat32 dOrientation,
			dbox::F32 dWeaponOrientation, dbox::F32 dHealth, dbox::F32 dShield) :
		Acceleration(dAcceleration), Velocity(dVelocity), Orientation(dOrientation), WeaponOrientation(dWeaponOrientation), Health(dHealth), Shield(dShield) {}
};

struct ConfigUpdate {
	dbox::I32	WeaponID1;
	dbox::I32	WeaponID2;
	dbox::I32	Equipped1;
	dbox::I32	Equipped2;
	dbox::F32	MaxHealth;
	dbox::F32	MaxShield;

	DBOX_STRUCTINFO_BEGIN()
		DBOX_STRUCTINFO_FIELD(ConfigUpdate, WeaponID1, dbox::FT_INT32, dbox::FM_CONFIG_CUSTOM_01)
		DBOX_STRUCTINFO_FIELD(ConfigUpdate, WeaponID2, dbox::FT_INT32, dbox::FM_CONFIG_CUSTOM_02)
		DBOX_STRUCTINFO_FIELD(ConfigUpdate, Equipped1, dbox::FT_INT32, dbox::FM_CONFIG_CUSTOM_03)
		DBOX_STRUCTINFO_FIELD(ConfigUpdate, Equipped2, dbox::FT_INT32, dbox::FM_CONFIG_CUSTOM_04)
		DBOX_STRUCTINFO_FIELD(ConfigUpdate, MaxHealth, dbox::FT_FLOAT32, dbox::FM_CONFIG_CUSTOM_05)
		DBOX_STRUCTINFO_FIELD(ConfigUpdate, MaxShield, dbox::FT_FLOAT32, dbox::FM_CONFIG_CUSTOM_06)
		DBOX_STRUCTINFO_END()

		ConfigUpdate(dbox::I32 nWeaponID1, dbox::I32 nWeaponID2, dbox::I32 nEquipped1, dbox::I32 nEquipped2, dbox::F32 dMaxHealth, dbox::F32 dMaxShield) :
		WeaponID1(nWeaponID1), WeaponID2(nWeaponID2), Equipped1(nEquipped1), Equipped2(nEquipped2), MaxHealth(dMaxHealth), MaxShield(dMaxShield) {}
};

// IMPACT, EXPLOSION
struct ImpactExplosion {
	dbox::I32	ImpactType;
	dbox::F32	Intensity;
	dbox::F32	Orientation;
	dbox::F32	Distance;

	DBOX_STRUCTINFO_BEGIN()
		DBOX_STRUCTINFO_FIELD(ImpactExplosion, ImpactType, dbox::FT_INT32, dbox::FM_ITEM_ID)
		DBOX_STRUCTINFO_FIELD(ImpactExplosion, Intensity, dbox::FT_FLOAT32, dbox::FM_EVENT_INTENSITY)
		DBOX_STRUCTINFO_FIELD(ImpactExplosion, Orientation, dbox::FT_FLOAT32, dbox::FM_YAW_RAD)
		DBOX_STRUCTINFO_FIELD(ImpactExplosion, Distance, dbox::FT_FLOAT32, dbox::FM_DISTANCE_FROM_PLAYER_M)
		DBOX_STRUCTINFO_END()

		ImpactExplosion(dbox::I32 nImpactType, dbox::F32 dIntensity, dbox::F32 dOrientation, dbox::F32 dDistance) :
		ImpactType(nImpactType), Intensity(dIntensity), Orientation(dOrientation), Distance(dDistance) {}
};