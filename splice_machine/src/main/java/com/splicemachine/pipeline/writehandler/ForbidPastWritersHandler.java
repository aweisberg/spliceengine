package com.splicemachine.pipeline.writehandler;

import com.splicemachine.hbase.KVPair;
import com.splicemachine.pipeline.api.WriteContext;
import com.splicemachine.pipeline.api.WriteHandler;
import com.splicemachine.pipeline.impl.WriteResult;
import com.splicemachine.si.impl.DDLFilter;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.util.List;

public class ForbidPastWritersHandler implements WriteHandler {
    private static final Logger LOG = Logger.getLogger(ForbidPastWritersHandler.class);

    private DDLFilter ddlFilter;

    public ForbidPastWritersHandler(DDLFilter ddlFilter) {
        super();
        this.ddlFilter = ddlFilter;
    }

    @Override
    public void next(KVPair mutation, WriteContext ctx) {
        try {
            if (!ddlFilter.isVisibleBy(ctx.getTxn())) {
                ctx.failed(mutation, WriteResult.failed("Writes forbidden by transaction " + ddlFilter.getTransaction()));
            }
        } catch (IOException e) {
            LOG.error("Couldn't asses the visibility of the DDL operation", e);
            ctx.failed(mutation, WriteResult.failed(e.getMessage()));
        }
    }

    @Override
    public void next(List<KVPair> mutations, WriteContext ctx) {
        try {
            if (!ddlFilter.isVisibleBy(ctx.getTxn())) {
                for (KVPair mutation : mutations) {
                    ctx.failed(mutation, WriteResult.failed("Writes forbidden by transaction " + ddlFilter.getTransaction()));
                }
            }
        } catch (IOException e) {
            LOG.error("Couldn't asses the visibility of the DDL operation", e);
            for (KVPair mutation : mutations) {
                ctx.failed(mutation, WriteResult.failed(e.getMessage()));
            }
        }
    }

    @Override
    public void flush(WriteContext ctx) throws IOException {
        // no op
    }

	@Override
	public void close(WriteContext ctx) throws IOException {
		// no op
	}


}