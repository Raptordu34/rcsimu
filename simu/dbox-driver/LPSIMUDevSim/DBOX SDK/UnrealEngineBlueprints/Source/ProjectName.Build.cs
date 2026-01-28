using System.IO;
using UnrealBuildTool;

public class Project_Name : ModuleRules
{
	public Project_Name(ReadOnlyTargetRules Target) : base(Target)
	{
		PCHUsage = PCHUsageMode.UseExplicitOrSharedPCHs;
	
		PublicDependencyModuleNames.AddRange(new string[] { "Core", "CoreUObject", "Engine", "InputCore" });

		PrivateDependencyModuleNames.AddRange(new string[] {  });

        // D-BOX SDK .libs
        PublicLibraryPaths.Add(LiveMotionSdkPath);
        if (Target.Platform == UnrealTargetPlatform.Win64)
        {
            PublicAdditionalLibraries.Add(Path.Combine(LiveMotionSdkPath, "dbxLive64MD-vc141.lib"));
        }
        else if (Target.Platform == UnrealTargetPlatform.Win32)
        {
            PublicAdditionalLibraries.Add(Path.Combine(LiveMotionSdkPath, "dbxLive32MD-vc141.lib"));
        }

        // Uncomment if you are using Slate UI
        // PrivateDependencyModuleNames.AddRange(new string[] { "Slate", "SlateCore" });

        // Uncomment if you are using online features
        // PrivateDependencyModuleNames.Add("OnlineSubsystem");

        // To include OnlineSubsystemSteam, add it to the plugins section in your uproject file with the Enabled attribute set to true
    }

    private string ModulePath
    {
        get { return ModuleDirectory; }
    }

    private string LiveMotionSdkPath
    {
        get { return Path.GetFullPath(Path.Combine(ModulePath, "../DBOX_SDK/lib/")); }
    }
}
