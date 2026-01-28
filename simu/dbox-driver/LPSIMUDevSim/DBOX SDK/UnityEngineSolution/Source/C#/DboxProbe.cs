namespace dbox
{
    using UnityEngine;
    using UnityEditor;
    using System.Collections;
    using System.Runtime.InteropServices;

    // Invoke calls to the DboxSdkWrapper.dll
    static public class DboxSdkWrapper
    {
        // D-BOX Manager Methods
        [DllImport(@"DboxSdkWrapper.DLL")]
        public static extern int InitializeDbox();

        [DllImport(@"DboxSdkWrapper.DLL")]
        public static extern int TerminateDbox();

        [DllImport(@"DboxSdkWrapper.DLL")]
        public static extern int OpenDbox();

        [DllImport(@"DboxSdkWrapper.DLL")]
        public static extern int CloseDbox();

        [DllImport(@"DboxSdkWrapper.DLL")]
        public static extern int StartDbox();

        [DllImport(@"DboxSdkWrapper.DLL")]
        public static extern int StopDbox();

        [DllImport(@"DboxSdkWrapper.DLL")]
        public static extern int ResetState();

        [DllImport(@"DboxSdkWrapper.DLL")]
        public static extern bool IsInitialized();

        [DllImport(@"DboxSdkWrapper.DLL")]
        public static extern bool IsOpened();

        [DllImport(@"DboxSdkWrapper.DLL")]
        public static extern bool IsStarted();

        // FRAME_UPDATES
        [DllImport(@"DboxSdkWrapper.DLL")]
        public static extern int PostFrameUpdate([MarshalAs(UnmanagedType.Struct)] DboxStructs.FrameUpdate oFrameUpdate);

        // CONFIGURATION_UPDATES
        [DllImport(@"DboxSdkWrapper.DLL")]
        public static extern int PostConfigUpdate([MarshalAs(UnmanagedType.Struct)] DboxStructs.ConfigUpdate oConfigUpdate);

        // ACTIONS_WITH_DATA
        [DllImport(@"DboxSdkWrapper.DLL")]
        public static extern int PostActionWithData([MarshalAs(UnmanagedType.Struct)] DboxStructs.ImpactExplosion oExplosion);

        // ACTIONS
        [DllImport(@"DboxSdkWrapper.DLL")]
        public static extern int PostAction();
    }

    // DBOX FRAME_UPDATE, CONFIG_UPDATE and ACTION_WITH_DATA Structures definitions 
    public class DboxStructs
    {

        // Base type
        [StructLayout(LayoutKind.Sequential)]
        public struct XyzFloat
        {
            public float X;
            public float Y;
            public float Z;
        }

        // FRAME_UPDATE Structures
        [StructLayout(LayoutKind.Sequential)]
        public struct FrameUpdate
        {
            public XyzFloat Acceleration;
            public XyzFloat Velocity;
            public XyzFloat Orientation;
            public float WeaponOrientation;
            public float Health;
            public float Shield;
        }

        // CONFIGURATION_UPDATE Structures
        [StructLayout(LayoutKind.Sequential)]
        public struct ConfigUpdate
        {
            public int WeaponID1;
            public int WeaponID2;
            public int Equipped1;
            public int Equipped2;
            public float MaxHealth;
            public float MaxShield;
        }

        // ACTION_WITH_DATA Structure
        [StructLayout(LayoutKind.Sequential)]
        public struct ImpactExplosion
        {
            public int ImpactType;
            public float Intensity;
            public float Orientation;
            public float Distance;
        }
    }
}