/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trello.rxlifecycle.components.support;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.trello.rxlifecycle.FragmentEvent;
import com.trello.rxlifecycle.FragmentLifecycleProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static org.junit.Assert.assertFalse;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RxSupportFragmentLifecycleTest {

    private Observable<Object> observable;

    @Before
    public void setup() {
        observable = PublishSubject.create().asObservable();
    }

    @Test
    public void testRxFragment() {
        testLifecycle(new RxFragment());
        testBindUntilEvent(new RxFragment());
        testBindToLifecycle(new RxFragment());
    }

    @Test
    public void testRxDialogFragment() {
        testLifecycle(new RxDialogFragment());
        testBindUntilEvent(new RxDialogFragment());
        testBindToLifecycle(new RxDialogFragment());
    }

    @Test
    public void testRxAppCompatDialogFragment() {
        // Once Robolectric is less broken we could run these tests
        // Until then, these are identical to RxDialogFragment, so whatever.
        //
        // testLifecycle(new RxAppCompatDialogFragment());
        // testBindUntilEvent(new RxAppCompatDialogFragment());
        // testBindToLifecycle(new RxAppCompatDialogFragment());
    }

    private void testLifecycle(FragmentLifecycleProvider provider) {
        Fragment fragment = (Fragment) provider;
        startFragment(fragment);

        TestSubscriber<FragmentEvent> testSubscriber = new TestSubscriber<>();
        provider.lifecycle().skip(1).subscribe(testSubscriber);

        fragment.onAttach(null);
        fragment.onCreate(null);
        fragment.onViewCreated(null, null);
        fragment.onStart();
        fragment.onResume();
        fragment.onPause();
        fragment.onStop();
        fragment.onDestroyView();
        fragment.onDestroy();
        fragment.onDetach();

        testSubscriber.assertValues(
            FragmentEvent.ATTACH,
            FragmentEvent.CREATE,
            FragmentEvent.CREATE_VIEW,
            FragmentEvent.START,
            FragmentEvent.RESUME,
            FragmentEvent.PAUSE,
            FragmentEvent.STOP,
            FragmentEvent.DESTROY_VIEW,
            FragmentEvent.DESTROY,
            FragmentEvent.DETACH
        );
    }

    // Tests bindUntil for any given FragmentLifecycleProvider implementation
    private void testBindUntilEvent(FragmentLifecycleProvider provider) {
        Fragment fragment = (Fragment) provider;
        startFragment(fragment);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        observable.compose(provider.bindUntilEvent(FragmentEvent.STOP)).subscribe(testSubscriber);

        fragment.onAttach(null);
        assertFalse(testSubscriber.isUnsubscribed());
        fragment.onCreate(null);
        assertFalse(testSubscriber.isUnsubscribed());
        fragment.onViewCreated(null, null);
        assertFalse(testSubscriber.isUnsubscribed());
        fragment.onStart();
        assertFalse(testSubscriber.isUnsubscribed());
        fragment.onResume();
        assertFalse(testSubscriber.isUnsubscribed());
        fragment.onPause();
        assertFalse(testSubscriber.isUnsubscribed());
        fragment.onStop();
        testSubscriber.assertCompleted();
        testSubscriber.assertUnsubscribed();
    }

    // Tests bindToLifecycle for any given FragmentLifecycleProvider implementation
    private void testBindToLifecycle(FragmentLifecycleProvider provider) {
        Fragment fragment = (Fragment) provider;
        startFragment(fragment);

        fragment.onAttach(null);
        TestSubscriber<Object> attachTestSub = new TestSubscriber<>();
        observable.compose(provider.bindToLifecycle()).subscribe(attachTestSub);

        fragment.onCreate(null);
        assertFalse(attachTestSub.isUnsubscribed());
        TestSubscriber<Object> createTestSub = new TestSubscriber<>();
        observable.compose(provider.bindToLifecycle()).subscribe(createTestSub);

        fragment.onViewCreated(null, null);
        assertFalse(attachTestSub.isUnsubscribed());
        assertFalse(createTestSub.isUnsubscribed());
        TestSubscriber<Object> createViewTestSub = new TestSubscriber<>();
        observable.compose(provider.bindToLifecycle()).subscribe(createViewTestSub);

        fragment.onStart();
        assertFalse(attachTestSub.isUnsubscribed());
        assertFalse(createTestSub.isUnsubscribed());
        assertFalse(createViewTestSub.isUnsubscribed());
        TestSubscriber<Object> startTestSub = new TestSubscriber<>();
        observable.compose(provider.bindToLifecycle()).subscribe(startTestSub);

        fragment.onResume();
        assertFalse(attachTestSub.isUnsubscribed());
        assertFalse(createTestSub.isUnsubscribed());
        assertFalse(createViewTestSub.isUnsubscribed());
        assertFalse(startTestSub.isUnsubscribed());
        TestSubscriber<Object> resumeTestSub = new TestSubscriber<>();
        observable.compose(provider.bindToLifecycle()).subscribe(resumeTestSub);

        fragment.onPause();
        assertFalse(attachTestSub.isUnsubscribed());
        assertFalse(createTestSub.isUnsubscribed());
        assertFalse(createViewTestSub.isUnsubscribed());
        assertFalse(startTestSub.isUnsubscribed());
        resumeTestSub.assertCompleted();
        resumeTestSub.assertUnsubscribed();
        TestSubscriber<Object> pauseTestSub = new TestSubscriber<>();
        observable.compose(provider.bindToLifecycle()).subscribe(pauseTestSub);

        fragment.onStop();
        assertFalse(attachTestSub.isUnsubscribed());
        assertFalse(createTestSub.isUnsubscribed());
        assertFalse(createViewTestSub.isUnsubscribed());
        startTestSub.assertCompleted();
        startTestSub.assertUnsubscribed();
        pauseTestSub.assertCompleted();
        pauseTestSub.assertUnsubscribed();
        TestSubscriber<Object> stopTestSub = new TestSubscriber<>();
        observable.compose(provider.bindToLifecycle()).subscribe(stopTestSub);

        fragment.onDestroyView();
        assertFalse(attachTestSub.isUnsubscribed());
        assertFalse(createTestSub.isUnsubscribed());
        createViewTestSub.assertCompleted();
        createViewTestSub.assertUnsubscribed();
        stopTestSub.assertCompleted();
        stopTestSub.assertUnsubscribed();
        TestSubscriber<Object> destroyViewTestSub = new TestSubscriber<>();
        observable.compose(provider.bindToLifecycle()).subscribe(destroyViewTestSub);

        fragment.onDestroy();
        assertFalse(attachTestSub.isUnsubscribed());
        createTestSub.assertCompleted();
        createTestSub.assertUnsubscribed();
        destroyViewTestSub.assertCompleted();
        destroyViewTestSub.assertUnsubscribed();
        TestSubscriber<Object> destroyTestSub = new TestSubscriber<>();
        observable.compose(provider.bindToLifecycle()).subscribe(destroyTestSub);

        fragment.onDetach();
        attachTestSub.assertCompleted();
        attachTestSub.assertUnsubscribed();
        destroyTestSub.assertCompleted();
        destroyTestSub.assertUnsubscribed();
    }

    // Easier than making everyone create their own shadows
    private void startFragment(Fragment fragment) {
        Robolectric.setupActivity(FragmentActivity.class).getSupportFragmentManager()
            .beginTransaction()
            .add(fragment, null)
            .commit();
    }
}
