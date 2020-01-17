jQuery(function() {
	var tag = false, ox = 0, left = 0, bgleft = 0;
	jQuery('.progress_btn_ec').mousedown(function(e) {
		ox = e.pageX - left;
		tag = true;
	});

	jQuery(document).mouseup(function() {
		tag = false;
	});

	var progress_width = jQuery('.progress_ec').width();

	jQuery('.progress_ec').mousemove(
			function(e) {
				if (tag) {
					left = e.pageX - ox;
					if (left <= 0) {
						left = 0;
					} else if (left > progress_width) {
						left = progress_width;
					}

					jQuery(e).children(".progress_btn_ec").css('left', left);
					jQuery(e).find(".progress_bar_ec").width(left);
					jQuery(e).children(".text_ec").html(
							parseInt((left / progress_width) * 100));
				}
			});

	jQuery('.progress_bg_ec').click(
			function(e) {
				if (!tag) {
					bgleft = jQuery('.progress_bg_ec').offset().left;
					left = e.pageX - bgleft;
					if (left <= 0) {
						left = 0;
					} else if (left > progress_width) {
						left = progress_width;
					}
					jQuery('.progress_btn_ec').css('left', left);
					jQuery('.progress_bar_ec').animate({
						width : left
					}, progress_width);
					jQuery('.text_ec').html(
							parseInt((left / progress_width) * 100) );
				}
			});

	var ec1 = false;
	var ec2 = false;
	jQuery("#ec1").click(function() {
		if (!ec1) {
			ec1 = true;
			left += progress_width / 2;
			if (left > progress_width) {
				left = progress_width;
			}
		} else {
			ec1 = false;
			left -= progress_width / 2;
			if (left <= 0) {
				left = 0;
			}
		}

		jQuery('.progress_btn_ec').css('left', left);
		jQuery('.progress_bar_ec').animate({
			width : left
		}, progress_width);
		jQuery('.text_ec').html(parseInt((left / progress_width) * 100) );
	});

	jQuery("#ec2").click(function() {
		if (!ec2) {
			ec2 = true;
			left -= progress_width / 2;
			if (left <= 0) {
				left = 0;
			}
		} else {
			ec2 = false;
			left += progress_width / 2;
			if (left > progress_width) {
				left = progress_width;
			}
		}

		jQuery('.progress_btn_ec').css('left', left);
		jQuery('.progress_bar_ec').animate({
			width : left
		}, progress_width);
		jQuery('.text_ec').html(parseInt((left / progress_width) * 100));
	});

});

jQuery(function() {
	jQuery(".reduce_img_ec").click(function(e) {
		var left = jQuery('.progress_bar_ec').width();
		var progress_width = jQuery('.progress_ec').width();
		left = left - progress_width * 0.05;
		if (left <= 0) {
			left = 0;
		}
		jQuery('.progress_btn_ec').css('left', left);
		jQuery('.progress_bar_ec').animate({
			width : left
		}, progress_width);
		jQuery('.text_ec').html(parseInt((left / progress_width) * 100) );
	});

	jQuery(".add_img_ec").click(function(e) {
		var left = jQuery('.progress_bar_ec').width();
		var progress_width = jQuery('.progress_ec').width();
		left = left + progress_width * 0.05;
		if (left >= progress_width) {
			left = progress_width;
		}
		jQuery('.progress_btn_ec').css('left', left);
		jQuery('.progress_bar_ec').animate({
			width : left
		}, progress_width);
		jQuery('.text_ec').html(parseInt((left / progress_width) * 100) );
	});

});
// ===========================//
jQuery(function() {
	var tag = false, ox = 0, left = 0, bgleft = 0;
	jQuery('.progress_btn_tt').mousedown(function(e) {
		ox = e.pageX - left;
		tag = true;
	});

	jQuery(document).mouseup(function() {
		tag = false;
	});

	var progress_width = jQuery('.progress_tt').width();

	jQuery('.progress_tt').mousemove(
			function(e) {
				if (tag) {
					left = e.pageX - ox;
					if (left <= 0) {
						left = 0;
					} else if (left > progress_width) {
						left = progress_width;
					}

					jQuery(e).children(".progress_btn_tt").css('left', left);
					jQuery(e).find(".progress_bar_tt").width(left);
					jQuery(e).children(".text_tt").html(
							parseInt((left / progress_width) * 100) );
				}
			});

	jQuery('.progress_bg_tt').click(
			function(e) {
				if (!tag) {
					bgleft = jQuery('.progress_bg_tt').offset().left;
					left = e.pageX - bgleft;
					if (left <= 0) {
						left = 0;
					} else if (left > progress_width) {
						left = progress_width;
					}
					jQuery('.progress_btn_tt').css('left', left);
					jQuery('.progress_bar_tt').animate({
						width : left
					}, progress_width);
					jQuery('.text_tt').html(
							parseInt((left / progress_width) * 100));
				}
			});

	var time1 = false;
	var time2 = false;
	jQuery("#time1").click(function() {
		if (!time1) {
			time1 = true;
			left += progress_width / 2;
			if (left > progress_width) {
				left = progress_width;
			}
		} else {
			time1 = false;
			left -= progress_width / 2;
			if (left <= 0) {
				left = 0;
			}
		}
		jQuery('.progress_btn_tt').css('left', left);
		jQuery('.progress_bar_tt').animate({
			width : left
		}, progress_width);
		jQuery('.text_tt').html(parseInt((left / progress_width) * 100) );
	})

	jQuery("#time2").click(function() {
		if (!time2) {
			time2 = true;
			left -= progress_width / 2;
			if (left <= 0) {
				left = 0;
			}

		} else {
			time2 = false;
			left += progress_width / 2;
			if (left > progress_width) {
				left = progress_width;
			}

		}
		jQuery('.progress_btn_tt').css('left', left);
		jQuery('.progress_bar_tt').animate({
			width : left
		}, progress_width);
		jQuery('.text_tt').html(parseInt((left / progress_width) * 100));
	})
});

jQuery(function() {
	jQuery(".reduce_img_tt").click(function(e) {
		var left = jQuery('.progress_bar_tt').width();
		var progress_width = jQuery('.progress_tt').width();
		left = left - progress_width * 0.05;
		if (left <= 0) {
			left = 0;
		}
		jQuery('.progress_btn_tt').css('left', left);
		jQuery('.progress_bar_tt').animate({
			width : left
		}, progress_width);
		jQuery('.text_tt').html(parseInt((left / progress_width) * 100));
	});

	jQuery(".add_img_tt").click(function(e) {
		var left = jQuery('.progress_bar_tt').width();
		var progress_width = jQuery('.progress_tt').width();
		left = left + progress_width * 0.05;
		if (left >= progress_width) {
			left = progress_width;
		}
		jQuery('.progress_btn_tt').css('left', left);
		jQuery('.progress_bar_tt').animate({
			width : left
		}, progress_width);
		jQuery('.text_tt').html(parseInt((left / progress_width) * 100) );
	});

});
// ================//
jQuery(function() {
	var tag = false, ox = 0, left = 0, bgleft = 0;
	jQuery('.progress_btn_ss').mousedown(function(e) {
		ox = e.pageX - left;
		tag = true;
	});

	jQuery(document).mouseup(function() {
		tag = false;
	});

	var progress_width = jQuery('.progress_ss').width();

	jQuery('.progress_ss').mousemove(
			function(e) {
				if (tag) {
					left = e.pageX - ox;
					if (left <= 0) {
						left = 0;
					} else if (left > progress_width) {
						left = progress_width;
					}

					jQuery(e).children(".progress_btn_ss").css('left', left);
					jQuery(e).find(".progress_bar_ss").width(left);
					jQuery(e).children(".text_ss").html(
							parseInt((left / progress_width) * 100) );
				}
			});

	jQuery('.progress_bg_ss').click(
			function(e) {
				if (!tag) {
					bgleft = jQuery('.progress_bg_ss').offset().left;
					left = e.pageX - bgleft;
					if (left <= 0) {
						left = 0;
					} else if (left > progress_width) {
						left = progress_width;
					}
					jQuery('.progress_btn_ss').css('left', left);
					jQuery('.progress_bar_ss').animate({
						width : left
					}, progress_width);
					jQuery('.text_ss').html(
							parseInt((left / progress_width) * 100) );
				}
			});

	var fz1 = false;
	var fz2 = false;
	jQuery("#load-bearing1").click(function() {
		if (!fz1) {
			fz1 = true;
			left += progress_width / 2;
			if (left > progress_width) {
				left = progress_width;
			}
		} else {
			fz1 = false;
			left -= progress_width / 2;
			if (left <= 0) {
				left = 0;
			}
		}
		jQuery('.progress_btn_ss').css('left', left);
		jQuery('.progress_bar_ss').animate({
			width : left
		}, progress_width);
		jQuery('.text_ss').html(parseInt((left / progress_width) * 100) );

	});

	jQuery("#load-bearing2").click(function() {
		if (!fz2) {
			fz2 = true;
			left += progress_width / 2;
			if (left > progress_width) {
				left = progress_width;
			}
		} else {
			fz2 = false;
			left -= progress_width / 2;
			if (left <= 0) {
				left = 0;
			}
		}
		jQuery('.progress_btn_ss').css('left', left);
		jQuery('.progress_bar_ss').animate({
			width : left
		}, progress_width);
		jQuery('.text_ss').html(parseInt((left / progress_width) * 100) );

	});
});

jQuery(function() {
	jQuery(".reduce_img_ss").click(function(e) {
		var left = jQuery('.progress_bar_ss').width();
		var progress_width = jQuery('.progress_ss').width();
		left = left - progress_width * 0.05;
		if (left <= 0) {
			left = 0;
		}
		jQuery('.progress_btn_ss').css('left', left);
		jQuery('.progress_bar_ss').animate({
			width : left
		}, progress_width);
		jQuery('.text_ss').html(parseInt((left / progress_width) * 100) );
	});

	jQuery(".add_img_ss").click(function(e) {
		var left = jQuery('.progress_bar_ss').width();
		var progress_width = jQuery('.progress_ss').width();
		left = left + progress_width * 0.05;
		if (left >= progress_width) {
			left = progress_width;
		}
		jQuery('.progress_btn_ss').css('left', left);
		jQuery('.progress_bar_ss').animate({
			width : left
		}, progress_width);
		jQuery('.text_ss').html(parseInt((left / progress_width) * 100) );
	});
});
