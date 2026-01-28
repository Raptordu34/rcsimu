#include "DboxSdkWrapper.h"
#include "DbxStructureDefs.h"

#include <string>
#include <windows.h>

#include <iostream>
#include <fstream>
using namespace std;

#ifdef _MANAGED
	#pragma managed(push, off)
#endif

#define DLLEXPORT extern "C" __declspec(dllexport)

DboxSdkWrapper* GetDboxSdkWrapper() {
	static DboxSdkWrapper g_oDboxSdkWrapper;
	return &g_oDboxSdkWrapper;
}

BOOL APIENTRY DllMain(HMODULE /*hModule*/, DWORD  /*ul_reason_for_call*/, LPVOID /*lpReserved*/) {
	return TRUE;
}

#ifdef _MANAGED
	#pragma managed(pop)
#endif

// LIVE MOTION METHODS
DLLEXPORT int InitializeDbox() {
	return GetDboxSdkWrapper()->Initialize();
}

DLLEXPORT int TerminateDbox() {
	return GetDboxSdkWrapper()->Terminate();
}

DLLEXPORT int OpenDbox() {
	return GetDboxSdkWrapper()->Open();
}

DLLEXPORT int CloseDbox() {
	return GetDboxSdkWrapper()->Close();
}

DLLEXPORT int StartDbox() {
	return GetDboxSdkWrapper()->Start();
}

DLLEXPORT int StopDbox() {
	return GetDboxSdkWrapper()->Stop();
}

DLLEXPORT int ResetState() {
	return GetDboxSdkWrapper()->ResetState();
}

DLLEXPORT bool IsInitialized() {
	return GetDboxSdkWrapper()->IsInitialized();
}

DLLEXPORT bool IsOpened() {
	return GetDboxSdkWrapper()->IsOpened();
}

DLLEXPORT bool IsStarted() {
	return GetDboxSdkWrapper()->IsStarted();
}

// FRAME_UPDATE
DLLEXPORT int PostFrameUpdate(FrameUpdate oFrameUpdate) {
	return GetDboxSdkWrapper()->PostFrameUpdate(oFrameUpdate);
}

// CONFIGURATION UPDATE
DLLEXPORT int PostConfigUpdate(ConfigUpdate oConfigUpdate) {
	return GetDboxSdkWrapper()->PostConfigUpdate(oConfigUpdate);
}

// ACTION_WITH_DATA
DLLEXPORT int PostActionWithData(ImpactExplosion oExplosion) {
	return GetDboxSdkWrapper()->PostActionWithData(oExplosion);
}

// ACTIONS
DLLEXPORT int PostAction() {
	return GetDboxSdkWrapper()->PostAction();
}