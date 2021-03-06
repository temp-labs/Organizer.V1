(function () {

    const COLOR_PICKER_DATA_KEY = 'ColorPicker';
    const COLOR_ITEM_SELECTOR = '.jsColor';
    const CHANGE_COLOR_EVENT = 'change.color';

    function ColorPicker($parentElement, options) {

        var self = this;
        self.currentColor = options.initialColor;

        $parentElement.find(COLOR_ITEM_SELECTOR).click(function (e) {
            e.preventDefault();

            self.currentColor = $(this).css("color");

            $parentElement.trigger(CHANGE_COLOR_EVENT, {color: self.currentColor});
        });
    }

    ColorPicker.prototype = {
        getCurrentColor: function () {
            return this.currentColor;
        }
    };

    Utils.defineJQueryPlugin('colorPicker', COLOR_PICKER_DATA_KEY, ColorPicker);
})();