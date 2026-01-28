#pragma once

#include "../../DBOX_SDK/include/dboxLiveMotion.h"

enum DboxEvents {
	// FRAME
	PLAYER_FRAME,			// accel, velo, rotation, health, shield
	VEHICLE_FRAME,			// rpm, suspensions, surface ID, slip
	CINEMOTION_TIMECODE,	// playback time, cpu time

	// CONFIG
	PLAYER_CONFIG,			// weapon 1, weapon 2, equip 1, equip 2, max health, max shield, ID
	VEHICLE_CONFIG,			// RPM min, RPM max, cylinders, weapon configuration, ID

	// ACTION_WITH_DATA
	INTERACT_WITH_OBJECT,	// interactable object ID
	PICKUP_ITEM,			// item ID
	USE_ITEM,				// item ID
	IMPACT,					// type, orientation
	EXPLOSION,				// type, distance, orientation
	ID_CINEMOTION_START,	// motion ID
	ID_CINEMOTION_STOP,		// motion ID

	// ACTION
	FIRE1,
	FIRE2,
	ALT_FIRE1,
	ALT_FIRE2,
	RELOAD1,
	RELOAD2,
	MELEE,
	THROW,
	STOMP,
	CROUCH_START,
	CROUCH_STOP,
	JUMP,
	SECOND_JUMP,
	CHARGE_START,
	CHARGE_RELEASE,
	FIRE_START,
	FIRE_RELEASE,
	BOOST,
};

struct DboxPlayerFrame {
	dbox::XyzFloat64	Acceleration;
	dbox::XyzFloat64	Velocity;
	dbox::XyzFloat64	Orientation;
	dbox::F64			WeaponOrientation;
	dbox::F64			Health;
	dbox::F64			Shield;

	DBOX_STRUCTINFO_BEGIN()
	DBOX_STRUCTINFO_FIELD(DboxPlayerFrame, Acceleration, dbox::FT_XYZ_FLOAT64, dbox::FM_ACCELERATION_XYZ)
	DBOX_STRUCTINFO_FIELD(DboxPlayerFrame, Velocity, dbox::FT_XYZ_FLOAT64, dbox::FM_VELOCITY_XYZ)
	DBOX_STRUCTINFO_FIELD(DboxPlayerFrame, Orientation, dbox::FT_XYZ_FLOAT64, dbox::FM_EVENT_ORIENTATION_XYZ)
	DBOX_STRUCTINFO_FIELD(DboxPlayerFrame, WeaponOrientation, dbox::FT_FLOAT64, dbox::FM_YAW_RAD)
	DBOX_STRUCTINFO_FIELD(DboxPlayerFrame, Health, dbox::FT_FLOAT64, dbox::FM_FRAME_CUSTOM_01)
	DBOX_STRUCTINFO_FIELD(DboxPlayerFrame, Shield, dbox::FT_FLOAT64, dbox::FM_FRAME_CUSTOM_02)
	DBOX_STRUCTINFO_END()
};

struct DboxVehicleFrame {
	dbox::F64			RPM;
	dbox::QuadFloat64	Suspension;
	dbox::QuadFloat64	SurfaceID;
	dbox::QuadFloat64	TireSlip;

	DBOX_STRUCTINFO_BEGIN()
	DBOX_STRUCTINFO_FIELD(DboxVehicleFrame, RPM, dbox::FT_FLOAT64, dbox::FM_ENGINE_RPM)
	DBOX_STRUCTINFO_FIELD(DboxVehicleFrame, Suspension, dbox::FT_QUAD_FLOAT64, dbox::FM_SUSPENSION_TRAVEL_NORM_QUAD)
	DBOX_STRUCTINFO_FIELD(DboxVehicleFrame, SurfaceID, dbox::FT_QUAD_FLOAT64, dbox::FM_SURFACE_TYPE_ID)
	DBOX_STRUCTINFO_FIELD(DboxVehicleFrame, TireSlip, dbox::FT_QUAD_FLOAT64, dbox::FM_TIRE_SLIP_RATIO_QUAD)
	DBOX_STRUCTINFO_END()
};

struct DboxCinemotionTimecode {
	dbox::F64 PlaybackTimeSec;

	DBOX_STRUCTINFO_BEGIN()
	DBOX_STRUCTINFO_FIELD(DboxCinemotionTimecode, PlaybackTimeSec, dbox::FT_FLOAT64, dbox::FM_CINEMOTION_PLAYBACK_TIME_SEC)
	DBOX_STRUCTINFO_END()

	DboxCinemotionTimecode(dbox::F64 dPlaybackTimeSec) :
		PlaybackTimeSec(dPlaybackTimeSec) {}
};

struct DboxPlayerConfig {
	dbox::I64	WeaponID1;
	dbox::I64	WeaponID2;
	dbox::I64	Equipped1;
	dbox::I64	Equipped2;
	dbox::F64	MaxHealth;
	dbox::F64	MaxShield;

	DBOX_STRUCTINFO_BEGIN()
	DBOX_STRUCTINFO_FIELD(DboxPlayerConfig, WeaponID1, dbox::FT_INT64, dbox::FM_CONFIG_CUSTOM_01)
	DBOX_STRUCTINFO_FIELD(DboxPlayerConfig, WeaponID2, dbox::FT_INT64, dbox::FM_CONFIG_CUSTOM_02)
	DBOX_STRUCTINFO_FIELD(DboxPlayerConfig, Equipped1, dbox::FT_INT64, dbox::FM_CONFIG_CUSTOM_03)
	DBOX_STRUCTINFO_FIELD(DboxPlayerConfig, Equipped2, dbox::FT_INT64, dbox::FM_CONFIG_CUSTOM_04)
	DBOX_STRUCTINFO_FIELD(DboxPlayerConfig, MaxHealth, dbox::FT_FLOAT64, dbox::FM_CONFIG_CUSTOM_05)
	DBOX_STRUCTINFO_FIELD(DboxPlayerConfig, MaxShield, dbox::FT_FLOAT64, dbox::FM_CONFIG_CUSTOM_06)
	DBOX_STRUCTINFO_END()
};

struct DboxVehicleConfig {
	dbox::I64	MinRPM;
	dbox::I64	MaxRPM;
	dbox::I64	Cylinders;
	dbox::I64	VehicleID;

	DBOX_STRUCTINFO_BEGIN()
	DBOX_STRUCTINFO_FIELD(DboxVehicleConfig, MinRPM, dbox::FT_INT64, dbox::FM_ENGINE_RPM_IDLE)
	DBOX_STRUCTINFO_FIELD(DboxVehicleConfig, MaxRPM, dbox::FT_INT64, dbox::FM_ENGINE_RPM_MAX)
	DBOX_STRUCTINFO_FIELD(DboxVehicleConfig, Cylinders, dbox::FT_INT64, dbox::FM_ENGINE_CYLINDERS)
	DBOX_STRUCTINFO_FIELD(DboxVehicleConfig, VehicleID, dbox::FT_INT64, dbox::FM_VEHICLE_TYPE)
	DBOX_STRUCTINFO_END()
};

// INTERACT, PICKUP, CINEMOTION
struct DboxIdentifier {
	dbox::I64	IdentifierID;

	DBOX_STRUCTINFO_BEGIN()
	DBOX_STRUCTINFO_FIELD(DboxIdentifier, IdentifierID, dbox::FT_INT64, dbox::FM_IDENTIFIER)
	DBOX_STRUCTINFO_END()

	DboxIdentifier(dbox::I64 nIdentifierID) :
		IdentifierID(nIdentifierID) {}
};

// IMPACT, EXPLOSION
struct DboxImpactExplosion {
	dbox::I64	ImpactType;
	dbox::F64	Intensity;
	dbox::F64	Orientation;
	dbox::F64	Distance;

	DBOX_STRUCTINFO_BEGIN()
	DBOX_STRUCTINFO_FIELD(DboxImpactExplosion, ImpactType, dbox::FT_INT64, dbox::FM_IDENTIFIER)
	DBOX_STRUCTINFO_FIELD(DboxImpactExplosion, Intensity, dbox::FT_FLOAT64, dbox::FM_EVENT_INTENSITY)
	DBOX_STRUCTINFO_FIELD(DboxImpactExplosion, Orientation, dbox::FT_FLOAT64, dbox::FM_YAW_RAD)
	DBOX_STRUCTINFO_FIELD(DboxImpactExplosion, Distance, dbox::FT_FLOAT64, dbox::FM_DISTANCE_FROM_PLAYER_M)
	DBOX_STRUCTINFO_END()
};