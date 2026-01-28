#include "DboxLiveMotionHandler.h"

#define APP_KEY			"app_key"
#define APP_BUILD		1000

DboxLiveMotionHandler::DboxLiveMotionHandler() {
}

DboxLiveMotionHandler::~DboxLiveMotionHandler() {
}

// LIVE MOTION METHODS
int DboxLiveMotionHandler::Initialize() {

	dbox::ELiveMotionResult eResult = dbox::LiveMotion::Initialize(APP_KEY, APP_BUILD);
	if (dbox::LMR_SUCCESS == eResult) {

		dbox::LiveMotion::RegisterEvent(PLAYER_FRAME, dbox::EM_FRAME_UPDATE, DboxPlayerFrame::GetStructInfo());
		dbox::LiveMotion::RegisterEvent(VEHICLE_FRAME, dbox::EM_FRAME_UPDATE, DboxVehicleFrame::GetStructInfo());
		dbox::LiveMotion::RegisterEvent(CINEMOTION_TIMECODE, dbox::EM_FRAME_UPDATE, DboxCinemotionTimecode::GetStructInfo());

		dbox::LiveMotion::RegisterEvent(PLAYER_CONFIG, dbox::EM_CONFIG_UPDATE, DboxPlayerConfig::GetStructInfo());
		dbox::LiveMotion::RegisterEvent(VEHICLE_CONFIG, dbox::EM_CONFIG_UPDATE, DboxVehicleConfig::GetStructInfo());

		dbox::LiveMotion::RegisterEvent(INTERACT_WITH_OBJECT, dbox::EM_ACTION_TRIGGER_PULSE, DboxIdentifier::GetStructInfo());
		dbox::LiveMotion::RegisterEvent(PICKUP_ITEM, dbox::EM_ACTION_TRIGGER_PULSE, DboxIdentifier::GetStructInfo());
		dbox::LiveMotion::RegisterEvent(USE_ITEM, dbox::EM_ACTION_TRIGGER_PULSE, DboxIdentifier::GetStructInfo());
		dbox::LiveMotion::RegisterEvent(IMPACT, dbox::EM_ACTION_TRIGGER_PULSE, DboxImpactExplosion::GetStructInfo());
		dbox::LiveMotion::RegisterEvent(EXPLOSION, dbox::EM_ACTION_TRIGGER_PULSE, DboxImpactExplosion::GetStructInfo());
		dbox::LiveMotion::RegisterEvent(ID_CINEMOTION_START, dbox::EM_ACTION_TRIGGER_START, DboxIdentifier::GetStructInfo());
		dbox::LiveMotion::RegisterEvent(ID_CINEMOTION_STOP, dbox::EM_ACTION_TRIGGER_STOP, DboxIdentifier::GetStructInfo());

		dbox::LiveMotion::RegisterEvent(FIRE1, dbox::EM_ACTION_TRIGGER_PULSE);
		dbox::LiveMotion::RegisterEvent(FIRE2, dbox::EM_ACTION_TRIGGER_PULSE);
		dbox::LiveMotion::RegisterEvent(ALT_FIRE1, dbox::EM_ACTION_TRIGGER_PULSE);
		dbox::LiveMotion::RegisterEvent(ALT_FIRE2, dbox::EM_ACTION_TRIGGER_PULSE);
		dbox::LiveMotion::RegisterEvent(MELEE, dbox::EM_ACTION_TRIGGER_PULSE);
		dbox::LiveMotion::RegisterEvent(THROW, dbox::EM_ACTION_TRIGGER_PULSE);
		dbox::LiveMotion::RegisterEvent(STOMP, dbox::EM_ACTION_TRIGGER_PULSE);
		dbox::LiveMotion::RegisterEvent(CROUCH_START, dbox::EM_ACTION_TRIGGER_START);
		dbox::LiveMotion::RegisterEvent(CROUCH_STOP, dbox::EM_ACTION_TRIGGER_STOP);
		dbox::LiveMotion::RegisterEvent(JUMP, dbox::EM_ACTION_TRIGGER_PULSE);
		dbox::LiveMotion::RegisterEvent(SECOND_JUMP, dbox::EM_ACTION_TRIGGER_PULSE);
		dbox::LiveMotion::RegisterEvent(CHARGE_START, dbox::EM_ACTION_TRIGGER_START);
		dbox::LiveMotion::RegisterEvent(CHARGE_RELEASE, dbox::EM_ACTION_TRIGGER_STOP);
		dbox::LiveMotion::RegisterEvent(BOOST, dbox::EM_ACTION_TRIGGER_PULSE);
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::Terminate() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::Terminate();
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::Open() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::Open();
	if (dbox::LMR_SUCCESS == eResult) {
	}
	return 0;
}

int DboxLiveMotionHandler::Close() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::Close();
	if (dbox::LMR_SUCCESS == eResult) {
	}
	return 0;
}

int DboxLiveMotionHandler::Start() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::Start();
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::Stop() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::Stop();
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::ResetState() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::ResetState();
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::IsInitialized() {
	return dbox::LiveMotion::IsInitialized();
}

int DboxLiveMotionHandler::IsOpened() {
	return dbox::LiveMotion::IsOpened();
}

int DboxLiveMotionHandler::IsStarted() {
	return dbox::LiveMotion::IsStarted();
}

// FRAME_UPDATES
int DboxLiveMotionHandler::PostPlayerFrame(DboxPlayerFrame oPlayerFrame) {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(PLAYER_FRAME, oPlayerFrame);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostVehicleFrame(DboxVehicleFrame oVehicleFrame) {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(VEHICLE_FRAME, oVehicleFrame);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostCinemotionTime(float dClipTimecode) {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(CINEMOTION_TIMECODE,
		DboxCinemotionTimecode(dClipTimecode));
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

// CONFIG_UPDATES
int DboxLiveMotionHandler::PostPlayerConfig(DboxPlayerConfig oPlayerConfig) {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(PLAYER_CONFIG, oPlayerConfig);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostVehicleConfig(DboxVehicleConfig oVehicleConfig) {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(VEHICLE_CONFIG, oVehicleConfig);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

// ACTIONS_WITH_DATA
int DboxLiveMotionHandler::PostInteract(int nInteractID) {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(INTERACT_WITH_OBJECT, DboxIdentifier(nInteractID));
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostPickup(int nPickupID) {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(PICKUP_ITEM, DboxIdentifier(nPickupID));
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostImpact(DboxImpactExplosion oImpact) {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(IMPACT, oImpact);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostExplosion(DboxImpactExplosion oExplosion) {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(EXPLOSION, oExplosion);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostCinemotionStart(int nMotionBaseID) {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(ID_CINEMOTION_START, DboxIdentifier(nMotionBaseID));
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostCinemotionStop(int nMotionBaseID) {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(ID_CINEMOTION_STOP, DboxIdentifier(nMotionBaseID));
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

// ACTIONS
int DboxLiveMotionHandler::PostFire1() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(FIRE1);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostFire2() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(FIRE2);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostAltFire1() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(ALT_FIRE1);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostAltFire2() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(ALT_FIRE2);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostReload1() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(RELOAD1);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostReload2() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(RELOAD2);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostMelee() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(MELEE);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostThrow() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(THROW);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostStomp() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(STOMP);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostCrouchStart() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(CROUCH_START);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostCrouchStop() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(CROUCH_STOP);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostJump() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(JUMP);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostSecondJump() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(SECOND_JUMP);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostChargeStart() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(CHARGE_START);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostChargeRelease() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(CHARGE_RELEASE);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}

int DboxLiveMotionHandler::PostBoost() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(BOOST);
	if (dbox::LMR_SUCCESS == eResult) {
		return 1;
	}
	return 0;
}