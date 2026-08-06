package tech.kayys.wayang.cli;

import tech.kayys.wayang.gollek.sdk.WayangWorkbenchModel;
import tech.kayys.wayang.gollek.sdk.WorkspaceSnapshot;

interface WayangWorkbenchRenderer<T> {

    T render(WayangWorkbenchModel model, WorkspaceSnapshot workspace);
}
