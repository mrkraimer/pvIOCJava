/*
 * Automatically generated by jrpcgen 1.0.7 on 9/3/08 7:17 AM
 * jrpcgen is part of the "Remote Tea" ONC/RPC package for Java
 * See http://remotetea.sourceforge.net for details
 */
package org.epics.pvioc.pdrv.vxi11.rpc;
import java.io.IOException;

import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrAble;
import org.acplt.oncrpc.XdrDecodingStream;
import org.acplt.oncrpc.XdrEncodingStream;

public class Device_Link implements XdrAble {

    public int value;

    public Device_Link() {
    }

    public Device_Link(int value) {
        this.value = value;
    }

    public Device_Link(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeInt(value);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        value = xdr.xdrDecodeInt();
    }

}
// End of Device_Link.java