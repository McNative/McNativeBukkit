package org.mcnative.licensing;

import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.io.FileUtil;
import net.pretronic.libraries.utility.io.IORuntimeException;
import org.mcnative.licensing.utils.LicenseUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LicenseVerifier {

    /**
     * Read and verify a license.
     *
     * @param resourceId The id of the resource for witch the license was issued
     * @param publicKey The public key to validate the signature
     * @param info The server data
     * @return The verified license details
     */
    public static License verify(String resourceId, String publicKey,ServerInfo info){
        Validate.notNull(info.getLicenseLocation());
        try{
            License license = License.read(info.getLicenseLocation());
            if(!license.verify(resourceId,publicKey)) throw new LicenseNotValidException();
            return license;
        }catch (Exception exception){
            throw new LicenseNotValidException(exception);
        }
    }

    /**
     * Read and check a license, if the license is not available, a new license is fetched from the license server.
     *
     * @param resourceId The id of the resource for witch the license was issued
     * @param publicKey The public key to validate the signature
     * @param info The server data
     * @return The verified license details
     */
    public static License verifyOrCheckout(String resourceId, String publicKey,ServerInfo info){
        try{
            License license = null;
            if(info.getLicenseLocation().exists()){
                license = License.read(info.getLicenseLocation());
                if(license.shouldRefresh()) license = null;
            }
            if(license == null) license = checkout(resourceId,info);
            if(!license.verify(resourceId,publicKey)) throw new LicenseNotValidException();
            return license;
        }catch (Exception exception){
            throw new LicenseNotValidException(exception);
        }
    }

    private static License checkout(String resourceId, ServerInfo info){
        try {
            URLConnection connection = new URL(info.getLicenseServer().replace("{resourceId}",resourceId)).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept-Charset", "UTF-8");

            connection.setRequestProperty("DeviceId", LicenseUtil.getDeviceId());
            connection.setRequestProperty("ServerId",info.getServerId());
            connection.setRequestProperty("serverSecret",info.getServerSecret());

            InputStream response = connection.getInputStream();

            String content;
            try (Scanner scanner = new Scanner(response)) {
                content = scanner.useDelimiter("\\A").next();
            }

            License license = License.read(content);
            license.save(info.getLicenseLocation());
            response.close();
            return license;
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }
}
