//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Layer;
import playn.core.PlayN;
import playn.core.Pointer;

import pythagoras.f.IDimension;
import pythagoras.f.Point;

/**
 * The root of a display hierarchy. An application can have one or more roots, but they should not
 * overlap and will behave as if oblivious to one another's existence.
 */
public class Root extends Elements<Root>
{
    /**
     * Sizes this root element to its preferred size.
     */
    public Root pack () {
        IDimension psize = preferredSize(0, 0);
        setSize(psize.width(), psize.height());
        return this;
    }

    /**
     * Sizes this root element to the specified width and its preferred height.
     */
    public Root packToWidth (float width) {
        IDimension psize = preferredSize(width, 0);
        setSize(width, psize.height());
        return this;
    }

    /**
     * Sizes this root element to the specified height and its preferred width.
     */
    public Root packToHeight (float height) {
        IDimension psize = preferredSize(0, height);
        setSize(psize.width(), height);
        return this;
    }

    /**
     * Sets the size of this root element.
     */
    public Root setSize (float width, float height) {
        _size.setSize(width, height);
        invalidate();
        return this;
    }

    /**
     * Sets the size of this root element and its translation from its parent.
     */
    public Root setBounds (float x, float y, float width, float height) {
        setSize(width, height);
        layer.setTranslation(x, y);
        return this;
    }

    protected Root (Interface iface, Layout layout, Stylesheet sheet) {
        super(layout);
        setStylesheet(sheet);
        _iface = iface;

        // we receive all pointer events for a root in that root and then dispatch events via our
        // custom mechanism from there on down
        layer.setHitTester(new Layer.HitTester() {
            public Layer hitTest (Layer layer, Point p) {
                return (isVisible() && contains(p.x, p.y)) ? layer : null;
            }
        });

        // add a pointer listener for handling mouse events
        PlayN.pointer().addListener(layer, new Pointer.Listener() {
            public void onPointerStart (Pointer.Event event) {
                // clear focus; if the click is on the focused item, it'll get focus again
                _iface.clearFocus();
                // dispatch the event to the appropriate hit element
                Point p = new Point(event.localX(), event.localY());
                _active = hitTest(p);
                if (_active != null) _active.onPointerStart(event, p.x, p.y);
            }
            public void onPointerDrag (Pointer.Event event) {
                if (_active == null) return;
                Point p = Layer.Util.screenToLayer(_active.layer, event.localX(), event.localY());
                _active.onPointerDrag(event, p.x, p.y);
            }
            public void onPointerEnd (Pointer.Event event) {
                if (_active == null) return;
                Point p = Layer.Util.screenToLayer(_active.layer, event.localX(), event.localY());
                _active.onPointerEnd(event, p.x, p.y);
                _active = null;
            }
        });
    }

    @Override protected Root root () {
        return this;
    }

    protected final Interface _iface;
    protected boolean _valid;
    protected Element<?> _active;
}
