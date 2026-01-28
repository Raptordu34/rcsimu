#pragma once
#include "DbxStructureDefs.h"

class DboxSdkWrapper {
public:
	DboxSdkWrapper();
	~DboxSdkWrapper();

	// LIVE MOTION METHODS
	int Initialize();
	int Terminate();
	int Open();
	int Close();
	int Start();
	int Stop();
	int ResetState();
	bool IsInitialized();
	bool IsOpened();
	bool IsStarted();

	// Add Event Triggers and Updates for your experience

	// FRAME_UPDATES
	int PostFrameUpdate(FrameUpdate oFrameUpdate);

	// CONFIGURATION UPDATES
	int PostConfigUpdate(ConfigUpdate oConfigUpdate);

	// ACTIONS_WITH_DATA
	int PostActionWithData(ImpactExplosion oExplosion);

	// ACTIONS
	int PostAction();
};