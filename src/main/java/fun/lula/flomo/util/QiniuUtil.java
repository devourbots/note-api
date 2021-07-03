package fun.lula.flomo.util;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.model.BatchStatus;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import fun.lula.flomo.config.QiniuConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class QiniuUtil {
    @Resource
    QiniuConfig qiniuConfig;


    public String getFileUploadToken() {
        Auth auth = getAuth();
        String bucket = qiniuConfig.getBUCKET();
        StringMap putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"fileKey\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\"," +
                "\"fileSize\":$(fsize), \"name\":\"$(fname)\", \"uid\":\"$(x:uid)\", \"url\": \"" + qiniuConfig.getDOMAIN() + "${key}\"}");
        return auth.uploadToken(bucket, null, 3600, putPolicy);
    }

    private Auth getAuth() {
        return Auth.create(qiniuConfig.getAK(), qiniuConfig.getSK());
    }

    public void delFile(String fileKey) {
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.region0());
        String bucket = qiniuConfig.getBUCKET();
        Auth auth = getAuth();
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            bucketManager.delete(bucket, fileKey);
        } catch (QiniuException ex) {
            //如果遇到异常，说明删除失败
            System.err.println(ex.code());
            System.err.println(ex.response.toString());
        }
    }

    public void batchDelFile(String[] fileList) {
        Configuration cfg = new Configuration(Region.region0());
        String bucket = qiniuConfig.getBUCKET();
        Auth auth = getAuth();
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            BucketManager.BatchOperations batchOperations = new BucketManager.BatchOperations();
            batchOperations.addDeleteOp(bucket, fileList);
            Response response = bucketManager.batch(batchOperations);
            BatchStatus[] batchStatusList = response.jsonToObject(BatchStatus[].class);
            for (int i = 0; i < fileList.length; i++) {
                BatchStatus status = batchStatusList[i];
                String key = fileList[i];
                log.info(key + "\t");
                if (status.code == 200) {
                    log.info("批量删除成功");
                } else {
                    log.info(status.data.error);
                }
            }
        } catch (QiniuException ex) {
            log.info(ex.response.toString());
        }
    }
}
