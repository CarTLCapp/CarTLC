package modules;

import java.util.*;
import com.avaje.ebean.*;
import play.mvc.*;
import java.util.concurrent.*;
import play.libs.concurrent.HttpExecution;

public abstract class CalcHelper<T> {

    protected WorkerExecutionContext mContext;
    protected PagedList<T> mPagedList;
    protected List<T> mList;
    protected int mPosition;
    protected int mPass;
    protected String mSeparator = "|";

    public CalcHelper(WorkerExecutionContext ctx) {
        mContext = ctx;
    }

    protected PagedList<T> init(PagedList<T> list) {
        mPagedList = list;
        mList = mPagedList.getList();
        mPosition = -1;
        mPass = 0;
        return mPagedList;
    }

    protected abstract String calc(T item);

    protected abstract String getTag(T item);

    protected boolean repeat() {
        return false;
    }

    T getNext() {
        if (++mPosition >= mList.size()) {
            if (repeat()) {
                mPass++;
                mPosition = 0;
            } else {
                return null;
            }
        }
        return mList.get(mPosition);
    }

    public CompletionStage<Result> calcNext(Controller controller) {
        Executor myEc = HttpExecution.fromThread((Executor) mContext);
        T item = getNext();
        if (item == null) {
            return CompletableFuture.completedFuture(controller.noContent());
        }
        return CompletableFuture.completedFuture(calc(item)).thenApplyAsync(result -> {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(getTag(item));
            sbuf.append(mSeparator);
            sbuf.append(result);
            return controller.ok(sbuf.toString());
        }, myEc);
    }

}
