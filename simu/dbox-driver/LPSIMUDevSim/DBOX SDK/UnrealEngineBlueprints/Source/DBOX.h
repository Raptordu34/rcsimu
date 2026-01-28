// Fill out your copyright notice in the Description page of Project Settings.

#include "CoreMinimal.h"

#include "Kismet/BlueprintFunctionLibrary.h"
#include "DBOX.generated.h"

//------------------------------------------------------------------------
/// Structures for DBOX Base Types
//------------------------------------------------------------------------
USTRUCT(BlueprintType)
struct FQuadFloat {
	GENERATED_USTRUCT_BODY()
		UPROPERTY(BlueprintReadWrite, Category = "D-BOX|BaseType")
		float FrontLeft;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|BaseType")
		float FrontRight;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|BaseType")
		float BackLeft;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|BaseType")
		float BackRight;
};

USTRUCT(BlueprintType)
struct FXYZ {
	GENERATED_USTRUCT_BODY()
		UPROPERTY(BlueprintReadWrite, Category = "D-BOX|BaseType")
		float X;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|BaseType")
		float Y;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|BaseType")
		float Z;
};

//------------------------------------------------------------------------
/// Structures for DBOX FRAME_UPDATE Types
//------------------------------------------------------------------------
USTRUCT(BlueprintType)
struct FPlayerFrame {
	GENERATED_USTRUCT_BODY()
		UPROPERTY(BlueprintReadWrite, Category = "D-BOX|PlayerFrame")
		FXYZ Acceleration;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|PlayerFrame")
		FXYZ Velocity;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|PlayerFrame")
		FXYZ Orientation;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|PlayerFrame")
		float WeaponOrientation;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|PlayerFrame")
		float Health;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|PlayerFrame")
		float Shield;
};

USTRUCT(BlueprintType)
struct FVehicleFrame {
	GENERATED_USTRUCT_BODY()
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|VehicleFrame")
		float RPM;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|VehicleFrame")
		FQuadFloat Suspension;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|VehicleFrame")
		FQuadFloat SurfaceID;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|VehicleFrame")
		FQuadFloat TireSlip;
};

USTRUCT(BlueprintType)
struct FCinemotionTimecode {
	GENERATED_USTRUCT_BODY()
		UPROPERTY(BlueprintReadWrite, Category = "D-BOX|CinemotionTime")
		float PlaybackTimeSec;
};

//------------------------------------------------------------------------
/// Structures for DBOX CONFIGURATION_UPDATE Types
//------------------------------------------------------------------------
USTRUCT(BlueprintType)
struct FPlayerConfig {
	GENERATED_USTRUCT_BODY()
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|PlayerConfig")
		int WeaponID1;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|PlayerConfig")
		int WeaponID2;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|PlayerConfig")
		int Equipped1;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|PlayerConfig")
		int Equipped2;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|PlayerConfig")
		float MaxHealth;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|PlayerConfig")
		float MaxShield;
};

USTRUCT(BlueprintType)
struct FVehicleConfig {
	GENERATED_USTRUCT_BODY()
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|VehicleConfig")
		int MinRPM;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|VehicleConfig")
		int MaxRPM;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|VehicleConfig")
		int Cylinders;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|VehicleConfig")
		int VehicleID;
};

//------------------------------------------------------------------------
/// Structures for DBOX ACTION_WITH_DATA Types
//------------------------------------------------------------------------
USTRUCT(BlueprintType)
struct FImpactExplosion {
	GENERATED_USTRUCT_BODY()
		UPROPERTY(BlueprintReadWrite, Category = "DBOX|ImpactExplosion")
		int ImpactType;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|ImpactExplosion")
		float Intensity;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|ImpactExplosion")
		float Orientation;
	UPROPERTY(BlueprintReadWrite, Category = "D-BOX|ImpactExplosion")
		float Distance;
};

//------------------------------------------------------------------------
/// DBOX BLUEPRINTS CLASS
//------------------------------------------------------------------------
UCLASS()
class PROJECTNAME_API UDBOX : public UBlueprintFunctionLibrary {
	GENERATED_BODY()
public:

	// Blueprint accessible D-BOX Management methods.
	UFUNCTION(BlueprintCallable, Category = "D-BOX|Management")
		static bool InitializeDbox();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Management")
		static bool TerminateDbox();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Management")
		static bool OpenDbox();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Management")
		static bool CloseDbox();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Management")
		static bool StartDbox();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Management")
		static bool StopDbox();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Management")
		static bool ResetState();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Management")
		static bool IsInitialized();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Management")
		static bool IsOpen();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Management")
		static bool IsStarted();

	// Blueprint accessible D-BOX Frame Update methods.
	UFUNCTION(BlueprintCallable, Category = "D-BOX|Frame")
		static bool PostPlayerFrame(FPlayerFrame PlayerFrame);

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Frame")
		static bool PostVehicleFrame(FVehicleFrame VehicleFrame);

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Frame")
		static bool PostCinemotionTime(float ClipTimecode);

	// Blueprint accessible D-BOX Configuration Update methods.
	UFUNCTION(BlueprintCallable, Category = "D-BOX|Configuration")
		static bool PostPlayerConfig(FPlayerConfig PlayerConfig);

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Configuration")
		static bool PostVehicleConfig(FVehicleConfig VehicleConfig);

	// Blueprint accessible D-BOX Action With Data methods.
	UFUNCTION(BlueprintCallable, Category = "D-BOX|ActionWithData")
		static bool PostInteract(int InteractID);

	UFUNCTION(BlueprintCallable, Category = "D-BOX|ActionWithData")
		static bool PostPickup(int PickupID);

	UFUNCTION(BlueprintCallable, Category = "D-BOX|ActionWithData")
		static bool PostImpact(FImpactExplosion Impact);

	UFUNCTION(BlueprintCallable, Category = "D-BOX|ActionWithData")
		static bool PostExplosion(FImpactExplosion Explosion);

	UFUNCTION(BlueprintCallable, Category = "D-BOX|ActionWithData")
		static bool PostCinemotionStart(int MotionBaseID);

	UFUNCTION(BlueprintCallable, Category = "D-BOX|ActionWithData")
		static bool PostCinemotionStop(int MotionBaseID);

	// Blueprint accessible Action methods.
	UFUNCTION(BlueprintCallable, Category = "D-BOX|Action")
		static bool PostFire1();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Action")
		static bool PostFire2();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Action")
		static bool PostAltFire1();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Action")
		static bool PostAltFire2();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Action")
		static bool PostReload1();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Action")
		static bool PostReload2();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Action")
		static bool PostMelee();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Action")
		static bool PostThrow();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Action")
		static bool PostStomp();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Action")
		static bool PostCrouchStart();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Action")
		static bool PostCrouchStop();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Action")
		static bool PostJump();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Action")
		static bool PostSecondJump();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Action")
		static bool PostChargeStart();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Action")
		static bool PostChargeRelease();

	UFUNCTION(BlueprintCallable, Category = "D-BOX|Action")
		static bool PostBoost();
};
