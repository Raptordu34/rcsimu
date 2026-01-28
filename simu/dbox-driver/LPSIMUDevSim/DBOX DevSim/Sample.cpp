#include <cstdio>
#include <conio.h>
#include <Windows.h>
#include "LiveMotion/dboxLiveMotion.h"
#include <math.h> // For sin

#pragma comment(lib, DBOX_LIVEMOTION_LIB)

const char* const APP_KEY = "LPSIMUDevSim";
const dbox::U32 APP_BUILD = 1000;
const double PI = 3.141592654f;

struct SimConfig {
	dbox::F32 MasterGain;
	dbox::F32 MasterSpectrum;

	DBOX_STRUCTINFO_BEGIN()
	DBOX_STRUCTINFO_FIELD(SimConfig, MasterGain, dbox::FT_FLOAT32, dbox::FM_MASTER_GAIN_DB)
	DBOX_STRUCTINFO_FIELD(SimConfig, MasterSpectrum, dbox::FT_FLOAT32, dbox::FM_MASTER_SPECTRUM_DB)
	DBOX_STRUCTINFO_END()
};

struct MotionConfig {
	dbox::F32 EngineRpmIdle;
	dbox::F32 EngineRpmMax;
	dbox::F32 EngineTorqueMax;

	DBOX_STRUCTINFO_BEGIN()
	DBOX_STRUCTINFO_FIELD(MotionConfig, EngineRpmIdle, dbox::FT_FLOAT32, dbox::FM_ENGINE_RPM_IDLE)
	DBOX_STRUCTINFO_FIELD(MotionConfig, EngineRpmMax, dbox::FT_FLOAT32, dbox::FM_ENGINE_RPM_MAX)
	DBOX_STRUCTINFO_FIELD(MotionConfig, EngineTorqueMax, dbox::FT_FLOAT32, dbox::FM_ENGINE_TORQUE_MAX)
	DBOX_STRUCTINFO_END()
};

struct MotionData {
	dbox::F32 Roll;  // -1.0 to 1.0
	dbox::F32 Pitch;  // -1.0 to 1.0
	dbox::F32 Heave;  // -1.0 to 1.0
	dbox::F32 EngineRpm; // Depends on MotionConfig EngineRpmIdle and EngineRpmMax
	dbox::F32 EngineTorque; // Depends on MotionConfig EngineTorqueMax

	DBOX_STRUCTINFO_BEGIN()
	DBOX_STRUCTINFO_FIELD(MotionData, Roll, dbox::FT_FLOAT32, dbox::FM_RAW_ROLL)
	DBOX_STRUCTINFO_FIELD(MotionData, Pitch, dbox::FT_FLOAT32, dbox::FM_RAW_PITCH)
	DBOX_STRUCTINFO_FIELD(MotionData, Heave, dbox::FT_FLOAT32, dbox::FM_RAW_HEAVE)
	DBOX_STRUCTINFO_FIELD(MotionData, EngineRpm, dbox::FT_FLOAT32, dbox::FM_ENGINE_RPM)
	DBOX_STRUCTINFO_FIELD(MotionData, EngineTorque, dbox::FT_FLOAT32, dbox::FM_ENGINE_TORQUE)
	DBOX_STRUCTINFO_END()
};


/// These are your unique event ids that you'll use when calling PostEvent.
enum AppEvents {
	SIM_CONFIG = 1000,
	MOTION_CONFIG = 2000,
	MOTION_DATA = 3000,
};

/// This function is used to prevent possible error when the application ends before the normal execution.
/// This includes Closing the console window, CTRL-C, Windows Shutdown, Log Off...
bool WINAPI OnAbnormalTerminate(DWORD /*dwCtrlType*/) {
	dbox::LiveMotion::Terminate();
	return true;
}

int main(int , char* []) {
	SetConsoleCtrlHandler((PHANDLER_ROUTINE)OnAbnormalTerminate, true);

	// Create a sample sinus signal
	float adSinusSignal[1000]; // 1000 samples
	for (int nIndex = 0; nIndex < 1000; nIndex++) {
		adSinusSignal[nIndex] =  static_cast<float>(sin(2.0f*PI*nIndex/1000.0f));
	}

	// Initialization and registration should be done only once.
	dbox::LiveMotion::Initialize(APP_KEY, APP_BUILD);
	dbox::LiveMotion::RegisterEvent(SIM_CONFIG, dbox::EM_CONFIG_UPDATE, SimConfig::GetStructInfo());
	dbox::LiveMotion::RegisterEvent(MOTION_CONFIG, dbox::EM_CONFIG_UPDATE, MotionConfig::GetStructInfo());
	dbox::LiveMotion::RegisterEvent(MOTION_DATA, dbox::EM_FRAME_UPDATE, MotionData::GetStructInfo());

	// Registration completed, open motion output device.
	// Open can be called once, after end of registration.
	dbox::LiveMotion::Open();
	{
		// Motion System initialization
		Sleep(10000);

		// Sim Start
		// It is always good to ResetState before each session.
		dbox::LiveMotion::ResetState();

		SimConfig oSimConfig;
		oSimConfig.MasterGain = 0; // 0dB
		oSimConfig.MasterSpectrum = 0; // 0dB
		dbox::LiveMotion::PostEvent(SIM_CONFIG, oSimConfig);

		MotionConfig oMotionConfig;
		oMotionConfig.EngineRpmIdle = 750.4f;
		oMotionConfig.EngineRpmMax = 3420.2f;
		oMotionConfig.EngineTorqueMax = 447.42f;
		dbox::LiveMotion::PostEvent(MOTION_CONFIG, oMotionConfig);

		MotionData oMotionData;
		oMotionData.Roll = -0.3f;
		oMotionData.Pitch = 0.2f;
		oMotionData.Heave = 0.3f;
		oMotionData.EngineRpm = 1000;
		oMotionData.EngineTorque = 175;
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);
		// ...
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);
		// ...
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);
		// ...
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);

		// Start of sim, this will fade-in actual motion.
		dbox::LiveMotion::Start();

		oMotionData.Roll = 0;
		oMotionData.Pitch = 0;
		oMotionData.Heave = 0;
		oMotionData.EngineRpm = 0; // Mute RPM
		oMotionData.EngineTorque = 0;
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);

		// ...
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);
		// ...
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);

		Sleep(5000);

		// Simulate Sinus signal
		for (int nSinLoop = 0; nSinLoop < 5; nSinLoop++) { // Play sinus 5 times
			for (int nIndex = 0; nIndex < 1000; nIndex++) {
				oMotionData.Heave = adSinusSignal[nIndex]; // Simulate Heave
				oMotionData.Roll = adSinusSignal[nIndex]; // Simulate Roll
				oMotionData.Pitch = adSinusSignal[nIndex]; // Simulate Pitch
				dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);
				Sleep(5);
			}
		}

		// Continue posting Motion data...
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);
		// ...
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);
		// ...
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);
		// ...
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);

		Sleep(5000);

		oMotionData.Roll = 0.3f;
		oMotionData.Pitch = -0.2f;
		oMotionData.Heave = -0.3f;
		oMotionData.EngineRpm = 2000;
		oMotionData.EngineTorque = 195;
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);

		Sleep(5000);

		// Reset State
		oMotionData.Roll = 0;
		oMotionData.Pitch = 0;
		oMotionData.Heave = 0;
		oMotionData.EngineRpm = 0;
		oMotionData.EngineTorque = 0;
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);

		Sleep(5000);

		// Pause, this will fade-out motion.
		dbox::LiveMotion::Stop();

		// Resume from pause, this will fade-in actual motion.
		dbox::LiveMotion::Start();
		// ...
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);
		// ...
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);
		// ...
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);
		// ...
		dbox::LiveMotion::PostEvent(MOTION_DATA, oMotionData);

		// End of level, this will fade-out motion.
		dbox::LiveMotion::Stop();

		// Level End
	}
	// Close motion output device.
	dbox::LiveMotion::Close();
	// Terminate
	dbox::LiveMotion::Terminate();

	printf("\nEnded, press any key...\n");
	_getch();

	return 0;
}