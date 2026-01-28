#pragma once

#include "DboxDataStructureDefs.h"

class DboxLiveMotionHandler {
public:
	DboxLiveMotionHandler();
	~DboxLiveMotionHandler();

	// LIVE MOTION METHODS
	int Initialize();
	int Terminate();
	int Open();
	int Close();
	int Start();
	int Stop();
	int ResetState();
	int IsInitialized();
	int IsOpened();
	int IsStarted();

	// FRAME_UPDATES
	int PostPlayerFrame(DboxPlayerFrame oPlayerFrame);
	int PostVehicleFrame(DboxVehicleFrame oVehicleFrame);
	int PostCinemotionTime(float dClipTimecode);

	// CONFIGURATION UPDATES
	int PostPlayerConfig(DboxPlayerConfig oPlayerConfig);
	int PostVehicleConfig(DboxVehicleConfig oVehicleConfig);

	// ACTIONS_WITH_DATA
	int PostInteract(int nInteractID);
	int PostPickup(int nPickupID);
	int PostImpact(DboxImpactExplosion oImpact);
	int PostExplosion(DboxImpactExplosion oExplosion);
	int PostCinemotionStart(int nMotionBaseID);
	int PostCinemotionStop(int nMotionBaseID);

	// ACTIONS
	int PostFire1();
	int PostFire2();
	int PostAltFire1();
	int PostAltFire2();
	int PostReload1();
	int PostReload2();
	int PostMelee();
	int PostThrow();
	int PostStomp();
	int PostCrouchStart();
	int PostCrouchStop();
	int PostJump();
	int PostSecondJump();
	int PostChargeStart();
	int PostChargeRelease();
	int PostBoost();
};