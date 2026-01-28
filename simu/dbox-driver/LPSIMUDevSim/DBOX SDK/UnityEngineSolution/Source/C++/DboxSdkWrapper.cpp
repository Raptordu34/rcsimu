#include "DboxSdkWrapper.h"
#include "LiveMotion/dboxLiveMotion.h"
#include <Windows.h>
#include <stdio.h>

// Create your own APP_KEY
#define APP_KEY         "UnityWrapperExample"
#define APP_BUILD       1000

#pragma comment(lib, DBOX_LIVEMOTION_LIB)

DboxSdkWrapper::DboxSdkWrapper() {
}


DboxSdkWrapper::~DboxSdkWrapper() {
}

// LIVE MOTION METHODS
int DboxSdkWrapper::Initialize() {
	dbox::ELiveMotionResult eResult = dbox::LiveMotion::Initialize(APP_KEY, APP_BUILD);
	if (dbox::LMR_SUCCESS == eResult) {

		dbox::LiveMotion::RegisterEvent(FRAME_UPDATE, dbox::EM_FRAME_UPDATE, FrameUpdate::GetStructInfo());
		dbox::LiveMotion::RegisterEvent(CONFIG_UPDATE, dbox::EM_CONFIG_UPDATE, ConfigUpdate::GetStructInfo());
		dbox::LiveMotion::RegisterEvent(ACTION_WITH_DATA, dbox::EM_ACTION_TRIGGER_PULSE, ImpactExplosion::GetStructInfo());
		dbox::LiveMotion::RegisterEvent(ACTION, dbox::EM_ACTION_TRIGGER_PULSE);

		OutputDebugStringA("Initialize OK");
		return 1;
	}
	OutputDebugStringA("Initialize ERROR");
	return 0;
}

	int DboxSdkWrapper::Terminate() {
		dbox::ELiveMotionResult eResult = dbox::LiveMotion::Terminate();
		if (dbox::LMR_SUCCESS == eResult) {
			OutputDebugStringA("Terminate OK");
			return 1;
		}
		OutputDebugStringA("Terminate ERROR");
		return 0;
	}

	int DboxSdkWrapper::Open() {
		dbox::ELiveMotionResult eResult = dbox::LiveMotion::Open();
		if (dbox::LMR_SUCCESS == eResult) {
			OutputDebugStringA("Open OK");
		}
		OutputDebugStringA("Open ERROR");
		return 0;
	}

	int DboxSdkWrapper::Close() {
		dbox::ELiveMotionResult eResult = dbox::LiveMotion::Close();
		if (dbox::LMR_SUCCESS == eResult) {
			OutputDebugStringA("Close OK");
		}
		OutputDebugStringA("Close ERROR");
		return 0;
	}

	int DboxSdkWrapper::Start() {
		dbox::ELiveMotionResult eResult = dbox::LiveMotion::Start();
		if (dbox::LMR_SUCCESS == eResult) {
			OutputDebugStringA("Start OK");
			return 1;
		}
		OutputDebugStringA("Start ERROR");
		return 0;
	}

	int DboxSdkWrapper::Stop() {
		dbox::ELiveMotionResult eResult = dbox::LiveMotion::Stop();
		if (dbox::LMR_SUCCESS == eResult) {
			OutputDebugStringA("Stop OK");
			return 1;
		}
		OutputDebugStringA("Stop ERROR");
		return 0;
	}

	int DboxSdkWrapper::ResetState() {
		dbox::ELiveMotionResult eResult = dbox::LiveMotion::ResetState();
		if (dbox::LMR_SUCCESS == eResult) {
			OutputDebugStringA("ResetState OK");
			return 1;
		}
		OutputDebugStringA("ResetState ERROR");
		return 0;
	}

	bool DboxSdkWrapper::IsInitialized() {
		return dbox::LiveMotion::IsInitialized();
	}

	bool DboxSdkWrapper::IsOpened() {
		return dbox::LiveMotion::IsOpened();
	}

	bool DboxSdkWrapper::IsStarted() {
		return dbox::LiveMotion::IsStarted();
	}

	// FRAME_UPDATES
	int DboxSdkWrapper::PostFrameUpdate(FrameUpdate oFrameUpdate) {
		dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(FRAME_UPDATE, oFrameUpdate);
		if (dbox::LMR_SUCCESS == eResult) {
			OutputDebugStringA("PostFrameUpdate OK");
			return 1;
		}
		OutputDebugStringA("PostFrameUpdate ERROR");
		return 0;
	}

	// CONFIG_UPDATES
	int DboxSdkWrapper::PostConfigUpdate(ConfigUpdate oConfigUpdate) {
		dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(CONFIG_UPDATE, oConfigUpdate);
		if (dbox::LMR_SUCCESS == eResult) {
			OutputDebugStringA("PostConfigUpdate OK");
			return 1;
		}
		OutputDebugStringA("PostConfigUpdate ERROR");
		return 0;
	}

	// ACTIONS_WITH_DATA
	int DboxSdkWrapper::PostActionWithData(ImpactExplosion oExplosion) {
		dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(ACTION_WITH_DATA, oExplosion);
		if (dbox::LMR_SUCCESS == eResult) {
			OutputDebugStringA("PostActionWithData OK");
			return 1;
		}
		OutputDebugStringA("PostActionWithData ERROR");
		return 0;
	}

	// ACTIONS
	int DboxSdkWrapper::PostAction() {
		dbox::ELiveMotionResult eResult = dbox::LiveMotion::PostEvent(ACTION);
		if (dbox::LMR_SUCCESS == eResult) {
			OutputDebugStringA("PostAction OK");
			return 1;
		}
		OutputDebugStringA("PostAction ERROR");
		return 0;
	}