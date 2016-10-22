package mx.xperience.ota.updater.server;

/**
 * Created by xXxRe on 22/10/2016.
 */

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import mx.xperience.ota.Version;
import mx.xperience.ota.updater.Server;
import mx.xperience.ota.updater.UpdatePackage;
import mx.xperience.ota.updater.Updater.PackageInfo;

public class BasketServer implements Server {

    private static final String URL = "https://basketbuild.com/devs/klozz/XPerience-rom/%s/11";

    private String mDevice = null;
    private String mError = null;
    private Version mVersion;
    private boolean mIsRom;

    public BasketServer(boolean isRom) {
        mIsRom = isRom;
    }

    @Override
    public String getUrl(String device, Version version) {
        mDevice = device;
        mVersion = version;
        return String.format(URL, new Object[] { device });
    }

    @Override
    public List<PackageInfo> createPackageInfoList(JSONObject response) throws Exception {
        mError = null;
        List<PackageInfo> list = new ArrayList<PackageInfo>();
        mError = response.optString("error");
        if (mError == null || mError.isEmpty()) {
            JSONArray updates = response.getJSONArray("updates");
            for (int i = updates.length() - 1; i >= 0; i--) {
                JSONObject file = updates.getJSONObject(i);
                String filename = file.optString("name");
                String stripped = filename.replace(".zip", "");
                stripped = stripped.replace("-signed", "");
                stripped = stripped.replace("-modular", "");
                String[] parts = stripped.split("-");
                int part = parts.length - 2;
                if (parts[part].startsWith("RC")) {
                    part = parts.length - 1;
                }
                boolean isNew = parts[parts.length - 1].matches("[-+]?\\d*\\.?\\d+");
                if (!isNew) {
                    continue;
                }
                Version version = new Version(filename);
                if (Version.compare(mVersion, version) < 0) {
                    list.add(new UpdatePackage(mDevice, filename, version, file.getString("size"),
                            file.getString("url"), file.getString("md5"), mIsRom));
                }
            }
        }
        return list;
    }

    @Override
    public String getError() {
        return mError;
    }

}