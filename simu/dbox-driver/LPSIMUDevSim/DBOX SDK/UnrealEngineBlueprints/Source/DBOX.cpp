// Fill out your copyright notice in the Description page of Project Settings.

#include "DBOX.h"

#include "DboxLiveMotionHandler.h"

DboxLiveMotionHandler* GetDboxLiveMotionHandler() {
	static DboxLiveMotionHandler g_oDboxLiveMotionHandler;
	return &g_oDboxLiveMotionHandler;
}

// Management Methods
bool UDBOX::InitializeDbox() {
	return GetDboxLiveMotionHandler()->Initialize() == 1; // 1 is OK
}

bool UDBOX::TerminateDbox() {
	return GetDboxLiveMotionHandler()->Terminate() == 1;
}

bool UDBOX::OpenDbox() {
	return GetDboxLiveMotionHandler()->Open() == 1;
}

bool UDBOX::CloseDbox() {
return GetDboxLiveMotionHandler()->Close() == 1;
}

bool UDBOX::StartDbox() {
	return GetDboxLiveMotionHandler()->Start() == 1;
}

bool UDBOX::StopDbox() {
	return GetDboxLiveMotionHandler()->Stop() == 1;
}

bool UDBOX::ResetState() {
	return GetDboxLiveMotionHandler()->ResetState() == 1;
}

bool UDBOX::IsInitialized() {
	return GetDboxLiveMotionHandler()->IsInitialized() == 1;
}

bool UDBOX::IsOpen() {
	return GetDboxLiveMotionHandler()->IsOpened() == 1;
}

bool UDBOX::IsStarted() {
	return GetDboxLiveMotionHandler()->IsStarted() == 1;
}

// Frame Update Methods
bool UDBOX::PostPlayerFrame(FPlayerFrame PlayerFrame) {

	DboxPlayerFrame oPlayerFrame;

	oPlayerFrame.Acceleration.X = PlayerFrame.Acceleration.X;
	oPlayerFrame.Acceleration.Y = PlayerFrame.Acceleration.Y;
	oPlayerFrame.Acceleration.Z = PlayerFrame.Acceleration.Z;
	oPlayerFrame.Velocity.X = PlayerFrame.Velocity.X;
	oPlayerFrame.Velocity.Y = PlayerFrame.Velocity.Y;
	oPlayerFrame.Velocity.Z = PlayerFrame.Velocity.Z;
	oPlayerFrame.Orientation.X = PlayerFrame.Orientation.X;
	oPlayerFrame.Orientation.Y = PlayerFrame.Orientation.Y;
	oPlayerFrame.Orientation.Z = PlayerFrame.Orientation.Z;
	oPlayerFrame.WeaponOrientation = PlayerFrame.WeaponOrientation;
	oPlayerFrame.Health = PlayerFrame.Health;
	oPlayerFrame.Shield = PlayerFrame.Shield;

	return GetDboxLiveMotionHandler()->PostPlayerFrame(oPlayerFrame) == 1;
}

bool UDBOX::PostVehicleFrame(FVehicleFrame VehicleFrame) {

	DboxVehicleFrame oVehicleFrame;

	oVehicleFrame.RPM = VehicleFrame.RPM;

	oVehicleFrame.Suspension.FrontLeft = VehicleFrame.Suspension.FrontLeft;
	oVehicleFrame.Suspension.FrontRight = VehicleFrame.Suspension.FrontRight;
	oVehicleFrame.Suspension.BackLeft = VehicleFrame.Suspension.BackLeft;
	oVehicleFrame.Suspension.BackRight = VehicleFrame.Suspension.BackRight;

	oVehicleFrame.SurfaceID.FrontLeft = VehicleFrame.SurfaceID.FrontLeft;
	oVehicleFrame.SurfaceID.FrontRight = VehicleFrame.SurfaceID.FrontRight;
	oVehicleFrame.SurfaceID.BackLeft = VehicleFrame.SurfaceID.BackLeft;
	oVehicleFrame.SurfaceID.BackRight = VehicleFrame.SurfaceID.BackRight;

	oVehicleFrame.TireSlip.FrontLeft = VehicleFrame.TireSlip.FrontLeft;
	oVehicleFrame.TireSlip.FrontRight = VehicleFrame.TireSlip.FrontRight;
	oVehicleFrame.TireSlip.BackLeft = VehicleFrame.TireSlip.BackLeft;
	oVehicleFrame.TireSlip.BackRight = VehicleFrame.TireSlip.BackRight;

	return GetDboxLiveMotionHandler()->PostVehicleFrame(oVehicleFrame) == 1;
}

bool UDBOX::PostCinemotionTime(float ClipTimecode) {
	return GetDboxLiveMotionHandler()->PostCinemotionTime(ClipTimecode) == 1;
}

// Configuration Update Methods
bool UDBOX::PostPlayerConfig(FPlayerConfig PlayerConfig) {

	DboxPlayerConfig oPlayerConfig;

	oPlayerConfig.WeaponID1 = PlayerConfig.WeaponID1;
	oPlayerConfig.WeaponID2 = PlayerConfig.WeaponID2;
	oPlayerConfig.Equipped1 = PlayerConfig.Equipped1;
	oPlayerConfig.Equipped2 = PlayerConfig.Equipped2;
	oPlayerConfig.MaxHealth = PlayerConfig.MaxHealth;
	oPlayerConfig.MaxShield = PlayerConfig.MaxShield;

	return GetDboxLiveMotionHandler()->PostPlayerConfig(oPlayerConfig) == 1;
}

bool UDBOX::PostVehicleConfig(FVehicleConfig VehicleConfig) {

	DboxVehicleConfig oVehicleConfig;

	oVehicleConfig.MinRPM = VehicleConfig.MinRPM;
	oVehicleConfig.MaxRPM = VehicleConfig.MaxRPM;
	oVehicleConfig.Cylinders = VehicleConfig.Cylinders;
	oVehicleConfig.VehicleID = VehicleConfig.VehicleID;

	return GetDboxLiveMotionHandler()->PostVehicleConfig(oVehicleConfig) == 1;
}

// Actions With Data Methods
bool UDBOX::PostInteract(int InteractableID) {
	return GetDboxLiveMotionHandler()->PostInteract(InteractableID) == 1;
}

bool UDBOX::PostPickup(int PickupID) {
	return GetDboxLiveMotionHandler()->PostPickup(PickupID) == 1;
}

bool UDBOX::PostImpact(FImpactExplosion Impact) {

	DboxImpactExplosion oImpact;

	oImpact.ImpactType = Impact.ImpactType;
	oImpact.Intensity = Impact.Intensity;
	oImpact.Orientation = Impact.Orientation;
	oImpact.Distance = Impact.Distance;

	return GetDboxLiveMotionHandler()->PostImpact(oImpact) == 1;
}

bool UDBOX::PostExplosion(FImpactExplosion Explosion) {

	DboxImpactExplosion oExplosion;

	oExplosion.ImpactType = Explosion.ImpactType;
	oExplosion.Intensity = Explosion.Intensity;
	oExplosion.Orientation = Explosion.Orientation;
	oExplosion.Distance = Explosion.Distance;

	return GetDboxLiveMotionHandler()->PostExplosion(oExplosion) == 1;
}

bool UDBOX::PostCinemotionStart(int MotionBaseID) {
	return GetDboxLiveMotionHandler()->PostCinemotionStart(MotionBaseID) == 1;
}

bool UDBOX::PostCinemotionStop(int MotionBaseID) {
	return GetDboxLiveMotionHandler()->PostCinemotionStop(MotionBaseID) == 1;
}

// Actions Methods
bool UDBOX::PostFire1() {
	return GetDboxLiveMotionHandler()->PostFire1() == 1;
}

bool UDBOX::PostFire2() {
	return GetDboxLiveMotionHandler()->PostFire2() == 1;
}

bool UDBOX::PostAltFire1() {
	return GetDboxLiveMotionHandler()->PostAltFire1() == 1;
}

bool UDBOX::PostAltFire2() {
	return GetDboxLiveMotionHandler()->PostAltFire2() == 1;
}

bool UDBOX::PostReload1() {
	return GetDboxLiveMotionHandler()->PostReload1() == 1;
}

bool UDBOX::PostReload2() {
	return GetDboxLiveMotionHandler()->PostReload2() == 1;
}

bool UDBOX::PostMelee() {
	return GetDboxLiveMotionHandler()->PostMelee() == 1;
}

bool UDBOX::PostThrow() {
	return GetDboxLiveMotionHandler()->PostThrow() == 1;
}

bool UDBOX::PostStomp() {
	return GetDboxLiveMotionHandler()->PostStomp() == 1;
}

bool UDBOX::PostCrouchStart() {
	return GetDboxLiveMotionHandler()->PostCrouchStart() == 1;
}

bool UDBOX::PostCrouchStop() {
	return GetDboxLiveMotionHandler()->PostCrouchStop() == 1;
}

bool UDBOX::PostJump() {
	return GetDboxLiveMotionHandler()->PostJump() == 1;
}

bool UDBOX::PostSecondJump() {
	return GetDboxLiveMotionHandler()->PostSecondJump() == 1;
}

bool UDBOX::PostChargeStart() {
	return GetDboxLiveMotionHandler()->PostChargeStart() == 1;
}

bool UDBOX::PostChargeRelease() {
	return GetDboxLiveMotionHandler()->PostChargeRelease() == 1;
}

bool UDBOX::PostBoost() {
	return GetDboxLiveMotionHandler()->PostBoost() == 1;
}